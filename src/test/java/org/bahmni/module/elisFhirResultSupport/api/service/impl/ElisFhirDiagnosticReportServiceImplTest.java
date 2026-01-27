package org.bahmni.module.elisFhirResultSupport.api.service.impl;

import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import org.bahmni.module.elisFhirResultSupport.api.domain.OrderObservations;
import org.bahmni.module.elisFhirResultSupport.api.helper.ObservationExtractor;
import org.bahmni.module.fhir2AddlExtension.api.dao.BahmniFhirDiagnosticReportDao;
import org.bahmni.module.fhir2AddlExtension.api.model.Attachment;
import org.bahmni.module.fhir2AddlExtension.api.model.FhirDiagnosticReportExt;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.Encounter;
import org.openmrs.EncounterProvider;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.model.FhirDiagnosticReport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ElisFhirDiagnosticReportServiceImplTest {

    private ElisFhirDiagnosticReportServiceImpl service;
    private ObservationExtractor observationExtractor;
    private BahmniFhirDiagnosticReportDao diagnosticReportDao;
    private Order order;
    private Encounter encounter;
    private Patient patient;
    private Concept orderConcept;

    @Before
    public void setUp() {
        observationExtractor = mock(ObservationExtractor.class);
        diagnosticReportDao = mock(BahmniFhirDiagnosticReportDao.class);
        service = new ElisFhirDiagnosticReportServiceImpl(observationExtractor, diagnosticReportDao);

        order = mock(Order.class);
        encounter = mock(Encounter.class);
        patient = mock(Patient.class);
        orderConcept = mock(Concept.class);

        when(order.getPatient()).thenReturn(patient);
        when(order.getConcept()).thenReturn(orderConcept);
        when(order.getUuid()).thenReturn("order-uuid");
        when(encounter.getUuid()).thenReturn("encounter-uuid");
        when(orderConcept.getUuid()).thenReturn("concept-uuid");
    }

    @Test
    public void shouldCreateNewDiagnosticReportWhenNoExistingReportFound() {
        OrderObservations orderObservations = createOrderObservations(Collections.emptyList(), Collections.emptySet());
        when(observationExtractor.extractObservationsAndAttachments(order, encounter)).thenReturn(orderObservations);
        when(diagnosticReportDao.getSearchResults(any())).thenReturn(Collections.emptyList());
        when(encounter.getEncounterProviders()).thenReturn(Collections.emptySet());

        service.createOrUpdateDiagnosticReport(order, encounter);

        ArgumentCaptor<FhirDiagnosticReportExt> reportCaptor = ArgumentCaptor.forClass(FhirDiagnosticReportExt.class);
        verify(diagnosticReportDao).createOrUpdate(reportCaptor.capture());

        FhirDiagnosticReportExt savedReport = reportCaptor.getValue();
        assertEquals(patient, savedReport.getSubject());
        assertEquals(encounter, savedReport.getEncounter());
        assertEquals(orderConcept, savedReport.getCode());
        assertTrue(savedReport.getOrders().contains(order));
    }

    @Test
    public void shouldUpdateExistingDiagnosticReport() {
        FhirDiagnosticReportExt existingReport = new FhirDiagnosticReportExt();
        existingReport.setUuid("existing-report-uuid");
        OrderObservations orderObservations = createOrderObservations(Collections.emptyList(), Collections.emptySet());

        when(observationExtractor.extractObservationsAndAttachments(order, encounter)).thenReturn(orderObservations);
        when(diagnosticReportDao.getSearchResults(any())).thenReturn(Collections.singletonList(existingReport));
        when(encounter.getEncounterProviders()).thenReturn(Collections.emptySet());

        service.createOrUpdateDiagnosticReport(order, encounter);

        ArgumentCaptor<FhirDiagnosticReportExt> reportCaptor = ArgumentCaptor.forClass(FhirDiagnosticReportExt.class);
        verify(diagnosticReportDao).createOrUpdate(reportCaptor.capture());

        FhirDiagnosticReportExt updatedReport = reportCaptor.getValue();
        assertEquals("existing-report-uuid", updatedReport.getUuid());
        assertEquals(patient, updatedReport.getSubject());
    }

    @Test
    public void shouldSetResultsFromOrderObservations() {
        Obs obs1 = mock(Obs.class);
        Obs obs2 = mock(Obs.class);
        List<Obs> results = Arrays.asList(obs1, obs2);
        OrderObservations orderObservations = createOrderObservations(results, Collections.emptySet());

        when(observationExtractor.extractObservationsAndAttachments(order, encounter)).thenReturn(orderObservations);
        when(diagnosticReportDao.getSearchResults(any())).thenReturn(Collections.emptyList());
        when(encounter.getEncounterProviders()).thenReturn(Collections.emptySet());

        service.createOrUpdateDiagnosticReport(order, encounter);

        ArgumentCaptor<FhirDiagnosticReportExt> reportCaptor = ArgumentCaptor.forClass(FhirDiagnosticReportExt.class);
        verify(diagnosticReportDao).createOrUpdate(reportCaptor.capture());

        FhirDiagnosticReportExt savedReport = reportCaptor.getValue();
        assertEquals(2, savedReport.getResults().size());
        assertTrue(savedReport.getResults().contains(obs1));
        assertTrue(savedReport.getResults().contains(obs2));
    }

    @Test
    public void shouldSetAttachmentsFromOrderObservations() {
        Attachment attachment1 = mock(Attachment.class);
        Attachment attachment2 = mock(Attachment.class);
        Set<Attachment> attachments = new HashSet<>(Arrays.asList(attachment1, attachment2));
        OrderObservations orderObservations = createOrderObservations(Collections.emptyList(), attachments);

        when(observationExtractor.extractObservationsAndAttachments(order, encounter)).thenReturn(orderObservations);
        when(diagnosticReportDao.getSearchResults(any())).thenReturn(Collections.emptyList());
        when(encounter.getEncounterProviders()).thenReturn(Collections.emptySet());

        service.createOrUpdateDiagnosticReport(order, encounter);

        ArgumentCaptor<FhirDiagnosticReportExt> reportCaptor = ArgumentCaptor.forClass(FhirDiagnosticReportExt.class);
        verify(diagnosticReportDao).createOrUpdate(reportCaptor.capture());

        FhirDiagnosticReportExt savedReport = reportCaptor.getValue();
        assertEquals(attachments, savedReport.getPresentedForms());
    }

    @Test
    public void shouldExtractPerformersFromEncounter() {
        Provider provider1 = mock(Provider.class);
        Provider provider2 = mock(Provider.class);
        EncounterProvider encounterProvider1 = mock(EncounterProvider.class);
        EncounterProvider encounterProvider2 = mock(EncounterProvider.class);

        when(encounterProvider1.getProvider()).thenReturn(provider1);
        when(encounterProvider2.getProvider()).thenReturn(provider2);
        when(encounter.getEncounterProviders()).thenReturn(new HashSet<>(Arrays.asList(encounterProvider1, encounterProvider2)));

        OrderObservations orderObservations = createOrderObservations(Collections.emptyList(), Collections.emptySet());
        when(observationExtractor.extractObservationsAndAttachments(order, encounter)).thenReturn(orderObservations);
        when(diagnosticReportDao.getSearchResults(any())).thenReturn(Collections.emptyList());

        service.createOrUpdateDiagnosticReport(order, encounter);

        ArgumentCaptor<FhirDiagnosticReportExt> reportCaptor = ArgumentCaptor.forClass(FhirDiagnosticReportExt.class);
        verify(diagnosticReportDao).createOrUpdate(reportCaptor.capture());

        FhirDiagnosticReportExt savedReport = reportCaptor.getValue();
        assertEquals(2, savedReport.getPerformers().size());
        assertTrue(savedReport.getPerformers().contains(provider1));
        assertTrue(savedReport.getPerformers().contains(provider2));
    }

    @Test
    public void shouldSkipNullProvidersWhenExtractingPerformers() {
        EncounterProvider encounterProviderWithProvider = mock(EncounterProvider.class);
        EncounterProvider encounterProviderWithoutProvider = mock(EncounterProvider.class);
        Provider provider = mock(Provider.class);

        when(encounterProviderWithProvider.getProvider()).thenReturn(provider);
        when(encounterProviderWithoutProvider.getProvider()).thenReturn(null);
        when(encounter.getEncounterProviders()).thenReturn(new HashSet<>(Arrays.asList(encounterProviderWithProvider, encounterProviderWithoutProvider)));

        OrderObservations orderObservations = createOrderObservations(Collections.emptyList(), Collections.emptySet());
        when(observationExtractor.extractObservationsAndAttachments(order, encounter)).thenReturn(orderObservations);
        when(diagnosticReportDao.getSearchResults(any())).thenReturn(Collections.emptyList());

        service.createOrUpdateDiagnosticReport(order, encounter);

        ArgumentCaptor<FhirDiagnosticReportExt> reportCaptor = ArgumentCaptor.forClass(FhirDiagnosticReportExt.class);
        verify(diagnosticReportDao).createOrUpdate(reportCaptor.capture());

        FhirDiagnosticReportExt savedReport = reportCaptor.getValue();
        assertEquals(1, savedReport.getPerformers().size());
        assertTrue(savedReport.getPerformers().contains(provider));
    }

    @Test
    public void shouldFindExistingReportByEncounterAndConcept() {
        FhirDiagnosticReportExt existingReport = new FhirDiagnosticReportExt();
        existingReport.setUuid("existing-report-uuid");

        when(diagnosticReportDao.getSearchResults(any())).thenReturn(Collections.singletonList(existingReport));

        FhirDiagnosticReportExt result = service.findExistingReport(encounter, order);

        assertNotNull(result);
        assertEquals("existing-report-uuid", result.getUuid());
        verify(diagnosticReportDao).getSearchResults(any(SearchParameterMap.class));
    }

    @Test
    public void shouldReturnNullWhenNoExistingReportFound() {
        when(diagnosticReportDao.getSearchResults(any())).thenReturn(Collections.emptyList());

        FhirDiagnosticReportExt result = service.findExistingReport(encounter, order);

        assertNull(result);
    }

    @Test
    public void shouldReturnNullWhenSearchThrowsException() {
        when(diagnosticReportDao.getSearchResults(any())).thenThrow(new RuntimeException("Database error"));

        FhirDiagnosticReportExt result = service.findExistingReport(encounter, order);

        assertNull(result);
    }

    @Test
    public void shouldSetStatusToPreliminaryWhenResultsAreEmpty() {
        OrderObservations orderObservations = createOrderObservations(Collections.emptyList(), Collections.emptySet());
        when(observationExtractor.extractObservationsAndAttachments(order, encounter)).thenReturn(orderObservations);
        when(diagnosticReportDao.getSearchResults(any())).thenReturn(Collections.emptyList());
        when(encounter.getEncounterProviders()).thenReturn(Collections.emptySet());

        service.createOrUpdateDiagnosticReport(order, encounter);

        ArgumentCaptor<FhirDiagnosticReportExt> reportCaptor = ArgumentCaptor.forClass(FhirDiagnosticReportExt.class);
        verify(diagnosticReportDao).createOrUpdate(reportCaptor.capture());

        FhirDiagnosticReportExt savedReport = reportCaptor.getValue();
        assertEquals(FhirDiagnosticReport.DiagnosticReportStatus.PRELIMINARY, savedReport.getStatus());
    }

    @Test
    public void shouldSetStatusToFinalWhenAllResultsHaveValues() {
        Obs obs1 = createObsWithValue(false);
        Obs obs2 = createObsWithValue(false);
        List<Obs> results = Arrays.asList(obs1, obs2);
        OrderObservations orderObservations = createOrderObservations(results, Collections.emptySet());

        when(observationExtractor.extractObservationsAndAttachments(order, encounter)).thenReturn(orderObservations);
        when(diagnosticReportDao.getSearchResults(any())).thenReturn(Collections.emptyList());
        when(encounter.getEncounterProviders()).thenReturn(Collections.emptySet());

        service.createOrUpdateDiagnosticReport(order, encounter);

        ArgumentCaptor<FhirDiagnosticReportExt> reportCaptor = ArgumentCaptor.forClass(FhirDiagnosticReportExt.class);
        verify(diagnosticReportDao).createOrUpdate(reportCaptor.capture());

        FhirDiagnosticReportExt savedReport = reportCaptor.getValue();
        assertEquals(FhirDiagnosticReport.DiagnosticReportStatus.FINAL, savedReport.getStatus());
    }

    @Test
    public void shouldSetStatusToPreliminaryWhenAnyObsIsVoided() {
        Obs obs1 = createObsWithValue(false);
        Obs obs2 = createObsWithValue(true);
        List<Obs> results = Arrays.asList(obs1, obs2);
        OrderObservations orderObservations = createOrderObservations(results, Collections.emptySet());

        when(observationExtractor.extractObservationsAndAttachments(order, encounter)).thenReturn(orderObservations);
        when(diagnosticReportDao.getSearchResults(any())).thenReturn(Collections.emptyList());
        when(encounter.getEncounterProviders()).thenReturn(Collections.emptySet());

        service.createOrUpdateDiagnosticReport(order, encounter);

        ArgumentCaptor<FhirDiagnosticReportExt> reportCaptor = ArgumentCaptor.forClass(FhirDiagnosticReportExt.class);
        verify(diagnosticReportDao).createOrUpdate(reportCaptor.capture());

        FhirDiagnosticReportExt savedReport = reportCaptor.getValue();
        assertEquals(FhirDiagnosticReport.DiagnosticReportStatus.PRELIMINARY, savedReport.getStatus());
    }

    @Test
    public void shouldSetStatusToPreliminaryWhenAnyObsHasNoValue() {
        Obs obsWithValue = createObsWithValue(false);
        Obs obsWithoutValue = mock(Obs.class);
        when(obsWithoutValue.getVoided()).thenReturn(false);
        when(obsWithoutValue.getValueNumeric()).thenReturn(null);
        when(obsWithoutValue.getValueText()).thenReturn(null);
        when(obsWithoutValue.getValueCoded()).thenReturn(null);
        when(obsWithoutValue.getValueComplex()).thenReturn(null);
        when(obsWithoutValue.getValueBoolean()).thenReturn(null);
        when(obsWithoutValue.getValueDatetime()).thenReturn(null);

        List<Obs> results = Arrays.asList(obsWithValue, obsWithoutValue);
        OrderObservations orderObservations = createOrderObservations(results, Collections.emptySet());

        when(observationExtractor.extractObservationsAndAttachments(order, encounter)).thenReturn(orderObservations);
        when(diagnosticReportDao.getSearchResults(any())).thenReturn(Collections.emptyList());
        when(encounter.getEncounterProviders()).thenReturn(Collections.emptySet());

        service.createOrUpdateDiagnosticReport(order, encounter);

        ArgumentCaptor<FhirDiagnosticReportExt> reportCaptor = ArgumentCaptor.forClass(FhirDiagnosticReportExt.class);
        verify(diagnosticReportDao).createOrUpdate(reportCaptor.capture());

        FhirDiagnosticReportExt savedReport = reportCaptor.getValue();
        assertEquals(FhirDiagnosticReport.DiagnosticReportStatus.PRELIMINARY, savedReport.getStatus());
    }

    @Test
    public void shouldSetStatusToPreliminaryWhenPanelOrderIsIncomplete() {
        Concept panelConcept = mock(Concept.class);
        ConceptClass labSetClass = mock(ConceptClass.class);
        Concept test1 = mock(Concept.class);
        Concept test2 = mock(Concept.class);
        Concept test3 = mock(Concept.class);

        when(labSetClass.getName()).thenReturn("LabSet");
        when(panelConcept.getConceptClass()).thenReturn(labSetClass);
        when(panelConcept.getSetMembers()).thenReturn(Arrays.asList(test1, test2, test3));
        when(order.getConcept()).thenReturn(panelConcept);

        Obs obs1 = createObsWithValue(false);
        when(obs1.getConcept()).thenReturn(test1);
        Obs obs2 = createObsWithValue(false);
        when(obs2.getConcept()).thenReturn(test2);

        List<Obs> results = Arrays.asList(obs1, obs2);
        OrderObservations orderObservations = createOrderObservations(results, Collections.emptySet());

        when(observationExtractor.extractObservationsAndAttachments(order, encounter)).thenReturn(orderObservations);
        when(diagnosticReportDao.getSearchResults(any())).thenReturn(Collections.emptyList());
        when(encounter.getEncounterProviders()).thenReturn(Collections.emptySet());

        service.createOrUpdateDiagnosticReport(order, encounter);

        ArgumentCaptor<FhirDiagnosticReportExt> reportCaptor = ArgumentCaptor.forClass(FhirDiagnosticReportExt.class);
        verify(diagnosticReportDao).createOrUpdate(reportCaptor.capture());

        FhirDiagnosticReportExt savedReport = reportCaptor.getValue();
        assertEquals(FhirDiagnosticReport.DiagnosticReportStatus.PRELIMINARY, savedReport.getStatus());
    }

    @Test
    public void shouldSetStatusToFinalWhenPanelOrderIsComplete() {
        Concept panelConcept = mock(Concept.class);
        ConceptClass labSetClass = mock(ConceptClass.class);
        Concept test1 = mock(Concept.class);
        Concept test2 = mock(Concept.class);

        when(labSetClass.getName()).thenReturn("LabSet");
        when(panelConcept.getConceptClass()).thenReturn(labSetClass);
        when(panelConcept.getSetMembers()).thenReturn(Arrays.asList(test1, test2));
        when(order.getConcept()).thenReturn(panelConcept);

        Obs obs1 = createObsWithValue(false);
        when(obs1.getConcept()).thenReturn(test1);
        Obs obs2 = createObsWithValue(false);
        when(obs2.getConcept()).thenReturn(test2);

        List<Obs> results = Arrays.asList(obs1, obs2);
        OrderObservations orderObservations = createOrderObservations(results, Collections.emptySet());

        when(observationExtractor.extractObservationsAndAttachments(order, encounter)).thenReturn(orderObservations);
        when(diagnosticReportDao.getSearchResults(any())).thenReturn(Collections.emptyList());
        when(encounter.getEncounterProviders()).thenReturn(Collections.emptySet());

        service.createOrUpdateDiagnosticReport(order, encounter);

        ArgumentCaptor<FhirDiagnosticReportExt> reportCaptor = ArgumentCaptor.forClass(FhirDiagnosticReportExt.class);
        verify(diagnosticReportDao).createOrUpdate(reportCaptor.capture());

        FhirDiagnosticReportExt savedReport = reportCaptor.getValue();
        assertEquals(FhirDiagnosticReport.DiagnosticReportStatus.FINAL, savedReport.getStatus());
    }

    @Test
    public void shouldHandlePanelWithMoreResultsThanSetMembers() {
        Concept panelConcept = mock(Concept.class);
        ConceptClass labSetClass = mock(ConceptClass.class);
        Concept test1 = mock(Concept.class);
        Concept test2 = mock(Concept.class);
        Concept additionalTest = mock(Concept.class);

        when(labSetClass.getName()).thenReturn("LabSet");
        when(panelConcept.getConceptClass()).thenReturn(labSetClass);
        when(panelConcept.getSetMembers()).thenReturn(Arrays.asList(test1, test2));
        when(order.getConcept()).thenReturn(panelConcept);

        Obs obs1 = createObsWithValue(false);
        when(obs1.getConcept()).thenReturn(test1);
        Obs obs2 = createObsWithValue(false);
        when(obs2.getConcept()).thenReturn(test2);
        Obs obs3 = createObsWithValue(false);
        when(obs3.getConcept()).thenReturn(additionalTest);

        List<Obs> results = Arrays.asList(obs1, obs2, obs3);
        OrderObservations orderObservations = createOrderObservations(results, Collections.emptySet());

        when(observationExtractor.extractObservationsAndAttachments(order, encounter)).thenReturn(orderObservations);
        when(diagnosticReportDao.getSearchResults(any())).thenReturn(Collections.emptyList());
        when(encounter.getEncounterProviders()).thenReturn(Collections.emptySet());

        service.createOrUpdateDiagnosticReport(order, encounter);

        ArgumentCaptor<FhirDiagnosticReportExt> reportCaptor = ArgumentCaptor.forClass(FhirDiagnosticReportExt.class);
        verify(diagnosticReportDao).createOrUpdate(reportCaptor.capture());

        FhirDiagnosticReportExt savedReport = reportCaptor.getValue();
        assertEquals(FhirDiagnosticReport.DiagnosticReportStatus.FINAL, savedReport.getStatus());
    }

    @Test
    public void shouldSetStatusToFinalForNonPanelOrderWithResults() {
        Concept simpleConcept = mock(Concept.class);
        when(simpleConcept.getSet()).thenReturn(false);
        when(order.getConcept()).thenReturn(simpleConcept);

        Obs obs = createObsWithValue(false);
        List<Obs> results = Collections.singletonList(obs);
        OrderObservations orderObservations = createOrderObservations(results, Collections.emptySet());

        when(observationExtractor.extractObservationsAndAttachments(order, encounter)).thenReturn(orderObservations);
        when(diagnosticReportDao.getSearchResults(any())).thenReturn(Collections.emptyList());
        when(encounter.getEncounterProviders()).thenReturn(Collections.emptySet());

        service.createOrUpdateDiagnosticReport(order, encounter);

        ArgumentCaptor<FhirDiagnosticReportExt> reportCaptor = ArgumentCaptor.forClass(FhirDiagnosticReportExt.class);
        verify(diagnosticReportDao).createOrUpdate(reportCaptor.capture());

        FhirDiagnosticReportExt savedReport = reportCaptor.getValue();
        assertEquals(FhirDiagnosticReport.DiagnosticReportStatus.FINAL, savedReport.getStatus());
    }

    private OrderObservations createOrderObservations(List<Obs> results, Set<Attachment> attachments) {
        return new OrderObservations(results, attachments);
    }

    private Obs createObsWithValue(boolean voided) {
        Obs obs = mock(Obs.class);
        when(obs.getVoided()).thenReturn(voided);
        when(obs.getValueNumeric()).thenReturn(12.5);
        return obs;
    }
}
