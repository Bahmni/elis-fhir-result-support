package org.bahmni.module.elisFhirResultSupport.api.helper;

import org.bahmni.module.elisFhirResultSupport.api.domain.OrderObservations;
import org.bahmni.module.fhir2AddlExtension.api.model.Attachment;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Context.class })
public class ObservationExtractorTest {

    private ObservationExtractor observationExtractor;
    private Order order;
    private Encounter encounter;
    private User authenticatedUser;

    @Before
    public void setUp() {
        observationExtractor = new ObservationExtractor();
        order = mock(Order.class);
        encounter = mock(Encounter.class);
        authenticatedUser = mock(User.class);

        PowerMockito.mockStatic(Context.class);
        when(Context.getLocale()).thenReturn(Locale.ENGLISH);
        when(Context.getAuthenticatedUser()).thenReturn(authenticatedUser);
    }

    @Test
    public void shouldReturnEmptyResultsWhenNoObservationsMatch() {
        when(encounter.getObsAtTopLevel(false)).thenReturn(new HashSet<>());

        OrderObservations result = observationExtractor.extractObservationsAndAttachments(order, encounter);

        assertTrue(result.getResults().isEmpty());
        assertTrue(result.getAttachments().isEmpty());
    }

    @Test
    public void shouldExtractObservationsForGivenOrder() {
        Obs obs = createObs("Hemoglobin", 12.5);
        when(obs.getOrder()).thenReturn(order);
        when(encounter.getObsAtTopLevel(false)).thenReturn(setOf(obs));

        OrderObservations result = observationExtractor.extractObservationsAndAttachments(order, encounter);

        assertEquals(1, result.getResults().size());
        assertTrue(result.getResults().contains(obs));
    }

    @Test
    public void shouldIgnoreObservationsWithDifferentOrder() {
        Obs obs = createObs("Hemoglobin", 12.5);
        when(obs.getOrder()).thenReturn(mock(Order.class));
        when(encounter.getObsAtTopLevel(false)).thenReturn(setOf(obs));

        OrderObservations result = observationExtractor.extractObservationsAndAttachments(order, encounter);

        assertTrue(result.getResults().isEmpty());
    }

    @Test
    public void shouldIgnoreObservationsWithoutValue() {
        Obs obs = createObs("Hemoglobin", null);
        when(obs.getValueNumeric()).thenReturn(null);
        when(obs.getValueText()).thenReturn(null);
        when(obs.getValueCoded()).thenReturn(null);
        when(obs.getValueComplex()).thenReturn(null);
        when(obs.getValueBoolean()).thenReturn(null);
        when(obs.getValueDatetime()).thenReturn(null);
        when(obs.getOrder()).thenReturn(order);
        when(encounter.getObsAtTopLevel(false)).thenReturn(setOf(obs));

        OrderObservations result = observationExtractor.extractObservationsAndAttachments(order, encounter);

        assertTrue(result.getResults().isEmpty());
    }

    @Test
    public void shouldExcludeAllObservationsOtherThanResult() {
        Obs maxNormal = createObs("LAB_MAXNORMAL", 100.0);
        Obs minNormal = createObs("LAB_MINNORMAL", 10.0);
        Obs notes = createObs("LAB_NOTES", "Some notes");
        Obs labAbnormal = createObs("LAB_ABNORMAL", "true");
        Obs referredOut = createObs("REFERRED_OUT", "referred");
        Obs labResult = createObs("LAB_RESULT", "result-value");
        when(labAbnormal.getOrder()).thenReturn(order);
        when(referredOut.getOrder()).thenReturn(order);
        when(labResult.getOrder()).thenReturn(order);
        when(maxNormal.getOrder()).thenReturn(order);
        when(minNormal.getOrder()).thenReturn(order);
        when(notes.getOrder()).thenReturn(order);
        when(encounter.getObsAtTopLevel(false)).thenReturn(setOf(maxNormal, minNormal, notes, labAbnormal, referredOut, labResult));

        OrderObservations result = observationExtractor.extractObservationsAndAttachments(order, encounter);

        assertTrue(result.getResults().isEmpty());
    }

    @Test
    public void shouldCreateAttachmentForLabReportObservation() {
        Concept orderConcept = createConcept("Complete Blood Count");
        when(order.getConcept()).thenReturn(orderConcept);

        Obs labReportObs = createLabReportObs("http://example.com/report.pdf");
        when(labReportObs.getOrder()).thenReturn(order);
        when(encounter.getObsAtTopLevel(false)).thenReturn(setOf(labReportObs));

        OrderObservations result = observationExtractor.extractObservationsAndAttachments(order, encounter);

        assertTrue(result.getResults().isEmpty());
        assertEquals(1, result.getAttachments().size());

        Attachment attachment = result.getAttachments().iterator().next();
        assertEquals("http://example.com/report.pdf", attachment.getContentUrl());
        assertEquals("Complete Blood Count", attachment.getTitle());
        assertEquals(authenticatedUser, attachment.getCreator());
        assertNotNull(attachment.getUuid());
        assertNotNull(attachment.getDateCreated());
        assertFalse(attachment.getVoided());
    }

    @Test
    public void shouldNotCreateAttachmentForLabReportWithoutUrl() {
        Obs labReportObs = mock(Obs.class);
        Concept concept = createConcept("LAB_REPORT");
        when(labReportObs.getConcept()).thenReturn(concept);
        when(labReportObs.getValueText()).thenReturn(null);
        when(labReportObs.getOrder()).thenReturn(order);
        when(labReportObs.hasGroupMembers()).thenReturn(false);
        when(encounter.getObsAtTopLevel(false)).thenReturn(setOf(labReportObs));

        OrderObservations result = observationExtractor.extractObservationsAndAttachments(order, encounter);

        assertTrue(result.getAttachments().isEmpty());
    }

    @Test
    public void shouldProcessGroupMemberObservations() {
        Obs parentObs = createObs("Panel Test", null);
        when(parentObs.getValueNumeric()).thenReturn(null);
        when(parentObs.getValueText()).thenReturn(null);
        when(parentObs.getValueCoded()).thenReturn(null);
        when(parentObs.getValueComplex()).thenReturn(null);
        when(parentObs.getValueBoolean()).thenReturn(null);
        when(parentObs.getValueDatetime()).thenReturn(null);
        when(parentObs.getOrder()).thenReturn(order);
        when(parentObs.hasGroupMembers()).thenReturn(true);

        Obs childObs1 = createObs("Test1", 10.0);
        Obs childObs2 = createObs("Test2", 20.0);
        when(parentObs.getGroupMembers()).thenReturn(setOf(childObs1, childObs2));
        when(encounter.getObsAtTopLevel(false)).thenReturn(setOf(parentObs));

        OrderObservations result = observationExtractor.extractObservationsAndAttachments(order, encounter);

        assertEquals(2, result.getResults().size());
    }

    @Test
    public void shouldHandleMixedObservationsAndAttachments() {
        Concept orderConcept = createConcept("Complete Panel");
        when(order.getConcept()).thenReturn(orderConcept);

        Obs regularObs = createObs("Hemoglobin", 12.5);
        when(regularObs.getOrder()).thenReturn(order);

        Obs labReportObs = createLabReportObs("http://example.com/report.pdf");
        when(labReportObs.getOrder()).thenReturn(order);
        when(encounter.getObsAtTopLevel(false)).thenReturn(setOf(regularObs, labReportObs));

        OrderObservations result = observationExtractor.extractObservationsAndAttachments(order, encounter);

        assertEquals(1, result.getResults().size());
        assertEquals(1, result.getAttachments().size());
        assertTrue(result.getResults().contains(regularObs));
    }

    private Obs createObs(String conceptName, Object value) {
        Obs obs = mock(Obs.class);
        Concept concept = createConcept(conceptName);
        when(obs.getConcept()).thenReturn(concept);

        if (value instanceof Double) {
            when(obs.getValueNumeric()).thenReturn((Double) value);
        } else if (value instanceof String) {
            when(obs.getValueText()).thenReturn((String) value);
        }

        when(obs.hasGroupMembers()).thenReturn(false);
        return obs;
    }

    private Obs createLabReportObs(String url) {
        Obs obs = mock(Obs.class);
        Concept concept = createConcept("LAB_REPORT");
        when(obs.getConcept()).thenReturn(concept);
        when(obs.getValueText()).thenReturn(url);
        when(obs.hasGroupMembers()).thenReturn(false);
        return obs;
    }

    private Concept createConcept(String name) {
        Concept concept = mock(Concept.class);
        ConceptName conceptName = mock(ConceptName.class);
        when(conceptName.getName()).thenReturn(name);
        when(concept.getName()).thenReturn(conceptName);
        when(concept.getFullySpecifiedName(Locale.ENGLISH)).thenReturn(conceptName);
        return concept;
    }

    private Set<Obs> setOf(Obs... obs) {
        Set<Obs> set = new HashSet<>();
        for (Obs o : obs) {
            set.add(o);
        }
        return set;
    }
}
