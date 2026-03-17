package org.bahmni.module.elisFhirResultSupport.api.domain;

import org.bahmni.module.fhir2addlextension.api.model.Attachment;
import org.junit.Test;
import org.openmrs.Obs;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class OrderObservationsTest {

    @Test
    public void shouldReturnResultsFromGetter() {
        Obs obs1 = mock(Obs.class);
        Obs obs2 = mock(Obs.class);
        List<Obs> results = Arrays.asList(obs1, obs2);
        Set<Attachment> attachments = new HashSet<>();

        OrderObservations orderObservations = new OrderObservations(results, attachments);

        assertEquals(results, orderObservations.getResults());
        assertEquals(2, orderObservations.getResults().size());
    }

    @Test
    public void shouldReturnAttachmentsFromGetter() {
        Attachment attachment1 = mock(Attachment.class);
        Attachment attachment2 = mock(Attachment.class);
        Set<Attachment> attachments = new HashSet<>(Arrays.asList(attachment1, attachment2));
        List<Obs> results = new ArrayList<>();

        OrderObservations orderObservations = new OrderObservations(results, attachments);

        assertEquals(attachments, orderObservations.getAttachments());
        assertEquals(2, orderObservations.getAttachments().size());
    }

    @Test
    public void shouldHandleEmptyResultsAndAttachments() {
        List<Obs> results = new ArrayList<>();
        Set<Attachment> attachments = new HashSet<>();

        OrderObservations orderObservations = new OrderObservations(results, attachments);

        assertNotNull(orderObservations.getResults());
        assertNotNull(orderObservations.getAttachments());
        assertTrue(orderObservations.getResults().isEmpty());
        assertTrue(orderObservations.getAttachments().isEmpty());
    }
}
