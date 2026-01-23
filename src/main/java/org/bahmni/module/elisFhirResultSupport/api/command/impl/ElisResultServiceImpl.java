package org.bahmni.module.elisFhirResultSupport.api.command.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.module.elisatomfeedclient.api.command.ELISResultPostSaveCommand;
import org.bahmni.module.elisFhirResultSupport.api.domain.OrderObservations;
import org.bahmni.module.elisFhirResultSupport.api.helper.ObservationExtractor;
import org.bahmni.module.fhir2AddlExtension.api.dao.BahmniFhirDiagnosticReportDao;
import org.bahmni.module.fhir2AddlExtension.api.model.FhirDiagnosticReportExt;
import org.openmrs.Encounter;
import org.openmrs.EncounterProvider;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Provider;
import org.openmrs.api.OrderService;
import org.openmrs.module.fhir2.model.FhirDiagnosticReport;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ElisResultServiceImpl implements ELISResultPostSaveCommand {

    private static final Logger logger = LogManager.getLogger(ElisResultServiceImpl.class);

    private final OrderService orderService;
    private final ObservationExtractor observationExtractor;
    private final BahmniFhirDiagnosticReportDao diagnosticReportDao;

    public ElisResultServiceImpl(OrderService orderService, ObservationExtractor observationExtractor,
                                 BahmniFhirDiagnosticReportDao diagnosticReportDao) {
        this.orderService = orderService;
        this.observationExtractor = observationExtractor;
        this.diagnosticReportDao = diagnosticReportDao;
    }

    @Override
    public void onResult(List<Encounter> encounters) {
        logger.info("Processing {} lab result encounters", encounters.size());

        for (Encounter encounter : encounters) {
            try {
                processEncounter(encounter);
            } catch (Exception e) {
                logger.error("Error processing encounter {}: {}", encounter.getUuid(), e.getMessage(), e);
            }
        }

        logger.info("Completed processing {} encounters", encounters.size());
    }

    private void processEncounter(Encounter encounter) {
        logger.info("Processing encounter: {}", encounter.getUuid());

        Set<Order> orders = extractOrdersFromEncounter(encounter);

        for (Order order : orders) {
            updateOrderFulfillerStatus(order);
            createFhirDiagnosticReport(order, encounter);
        }
    }

    private Set<Order> extractOrdersFromEncounter(Encounter encounter) {
        Set<Order> uniqueOrders = new HashSet<>();

        for (Obs obs : encounter.getAllObs()) {
            if (obs.getOrder() != null) {
                uniqueOrders.add(obs.getOrder());
            }
        }

        return uniqueOrders;
    }

    private void updateOrderFulfillerStatus(Order order) {
        if (order.getFulfillerStatus() == Order.FulfillerStatus.IN_PROGRESS) {
            logger.info("Order {} already has IN_PROGRESS status, skipping update", order.getUuid());
            return;
        }

        order.setFulfillerStatus(Order.FulfillerStatus.IN_PROGRESS);
        orderService.saveOrder(order, null);
        logger.info("Updated order {} status to IN_PROGRESS", order.getUuid());
    }

    private void createFhirDiagnosticReport(Order order, Encounter encounter) {
        OrderObservations orderObservations = observationExtractor.extractObservationsAndAttachments(order, encounter);

        FhirDiagnosticReportExt report = new FhirDiagnosticReportExt();
        report.setSubject(order.getPatient());
        report.setEncounter(encounter);
        Set<Order> orders = new HashSet<>();
        orders.add(order);
        report.setOrders(orders);
        report.setCode(order.getConcept());
        report.setResults(new HashSet<>(orderObservations.getResults()));
        report.setPerformers(extractPerformers(encounter));
        report.setPresentedForms(orderObservations.getAttachments());
        report.setStatus(determineStatus(orderObservations.getResults()));

        diagnosticReportDao.createOrUpdate(report);
        logger.info("Created FHIR diagnostic report for order {}", order.getUuid());
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

    private FhirDiagnosticReport.DiagnosticReportStatus determineStatus(List<Obs> results) {
        if (results.isEmpty()) {
            return FhirDiagnosticReport.DiagnosticReportStatus.PRELIMINARY;
        }

        for (Obs obs : results) {
            if (Boolean.TRUE.equals(obs.getVoided()) || !hasObsValue(obs)) {
                return FhirDiagnosticReport.DiagnosticReportStatus.PRELIMINARY;
            }
        }

        return FhirDiagnosticReport.DiagnosticReportStatus.FINAL;
    }

    private boolean hasObsValue(Obs obs) {
        return obs.getValueNumeric() != null ||
                obs.getValueText() != null ||
                obs.getValueCoded() != null ||
                obs.getValueComplex() != null ||
                obs.getValueBoolean() != null ||
                obs.getValueDatetime() != null;
    }
}
