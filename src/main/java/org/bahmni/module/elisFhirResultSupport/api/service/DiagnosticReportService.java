package org.bahmni.module.elisFhirResultSupport.api.service;

import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.module.elisFhirResultSupport.api.domain.OrderObservations;
import org.bahmni.module.elisFhirResultSupport.api.helper.ObservationExtractor;
import org.bahmni.module.elisFhirResultSupport.api.util.ObsUtils;
import org.bahmni.module.fhir2AddlExtension.api.dao.BahmniFhirDiagnosticReportDao;
import org.bahmni.module.fhir2AddlExtension.api.model.FhirDiagnosticReportExt;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterProvider;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Provider;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.model.FhirDiagnosticReport;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DiagnosticReportService {

    private static final Logger logger = LogManager.getLogger(DiagnosticReportService.class);

    private final ObservationExtractor observationExtractor;
    private final BahmniFhirDiagnosticReportDao diagnosticReportDao;

    public DiagnosticReportService(ObservationExtractor observationExtractor,
                                   BahmniFhirDiagnosticReportDao diagnosticReportDao) {
        this.observationExtractor = observationExtractor;
        this.diagnosticReportDao = diagnosticReportDao;
    }

    public void createOrUpdateDiagnosticReport(Order order, Encounter encounter) {
        OrderObservations orderObservations = observationExtractor.extractObservationsAndAttachments(order, encounter);

        FhirDiagnosticReportExt report = findExistingReport(encounter, order);
        
        if (report == null) {
            report = new FhirDiagnosticReportExt();
            logger.info("Creating new diagnostic report for order {}", order.getUuid());
        } else {
            logger.info("Updating existing diagnostic report {} for order {}", report.getUuid(), order.getUuid());
        }
        
        report.setSubject(order.getPatient());
        report.setEncounter(encounter);
        Set<Order> orders = new HashSet<>();
        orders.add(order);
        report.setOrders(orders);
        report.setCode(order.getConcept());
        report.setResults(new HashSet<>(orderObservations.getResults()));
        report.setPerformers(extractPerformers(encounter));
        report.setPresentedForms(orderObservations.getAttachments());
        report.setStatus(determineStatus(orderObservations.getResults(), order));

        diagnosticReportDao.createOrUpdate(report);
        logger.info("Saved FHIR diagnostic report for order {}", order.getUuid());
    }

    public FhirDiagnosticReportExt findExistingReport(Encounter encounter, Order order) {
        try {
            SearchParameterMap theParams = new SearchParameterMap();

            ReferenceAndListParam encounterRef = new ReferenceAndListParam()
                .addAnd(new ReferenceOrListParam()
                    .add(new ReferenceParam().setValue(encounter.getUuid())));
            theParams.addParameter(FhirConstants.ENCOUNTER_REFERENCE_SEARCH_HANDLER, encounterRef);

            TokenAndListParam conceptCode = new TokenAndListParam()
                .addAnd(new TokenOrListParam()
                    .add(new TokenParam().setValue(order.getConcept().getUuid())));
            theParams.addParameter(FhirConstants.CODED_SEARCH_HANDLER, conceptCode);
            
            List<FhirDiagnosticReportExt> results = diagnosticReportDao.getSearchResults(theParams);
            
            if (!results.isEmpty()) {
                logger.info("Found existing diagnostic report {} for encounter {} and concept {}", 
                    results.get(0).getUuid(), encounter.getUuid(), order.getConcept().getUuid());
            }
            
            return results.isEmpty() ? null : results.get(0);
        } catch (Exception e) {
            logger.warn("Error searching for existing diagnostic report: {}", e.getMessage());
            return null;
        }
    }

    private Set<Provider> extractPerformers(Encounter encounter) {
        Set<Provider> performers = new HashSet<>();
        for (EncounterProvider encounterProvider : encounter.getEncounterProviders()) {
            if (encounterProvider.getProvider() != null) {
                performers.add(encounterProvider.getProvider());
            }
        }
        return performers;
    }

    private FhirDiagnosticReport.DiagnosticReportStatus determineStatus(List<Obs> results, Order order) {
        if (results.isEmpty()) {
            return FhirDiagnosticReport.DiagnosticReportStatus.PRELIMINARY;
        }

        if (isPanelOrder(order) && !isPanelComplete(results, order)) {
            return FhirDiagnosticReport.DiagnosticReportStatus.PRELIMINARY;
        }

        for (Obs obs : results) {
            if (Boolean.TRUE.equals(obs.getVoided()) || !ObsUtils.hasValue(obs)) {
                return FhirDiagnosticReport.DiagnosticReportStatus.PRELIMINARY;
            }
        }

        return FhirDiagnosticReport.DiagnosticReportStatus.FINAL;
    }

    private boolean isPanelOrder(Order order) {
        Concept concept = order.getConcept();
        return concept != null && concept.getSet() != null && concept.getSet();
    }

    private boolean isPanelComplete(List<Obs> results, Order order) {
        List<Concept> panelTests = order.getConcept().getSetMembers();
        
        Set<Concept> completedTestConcepts = new HashSet<>();
        for (Obs obs : results) {
            if (panelTests.contains(obs.getConcept())) {
                completedTestConcepts.add(obs.getConcept());
            }
        }
        
        int totalTests = panelTests.size();
        int completedTests = completedTestConcepts.size();

        return completedTests >= totalTests;
    }
}
