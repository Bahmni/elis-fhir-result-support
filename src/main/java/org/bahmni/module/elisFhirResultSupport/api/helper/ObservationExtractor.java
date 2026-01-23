package org.bahmni.module.elisFhirResultSupport.api.helper;

import org.bahmni.module.elisFhirResultSupport.api.domain.OrderObservations;
import org.bahmni.module.fhir2AddlExtension.api.model.Attachment;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.api.context.Context;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class ObservationExtractor {

    private static final String LAB_REPORT_CONCEPT_NAME = "LAB_REPORT";

    public OrderObservations extractObservationsAndAttachments(Order order, Encounter encounter) {
        List<Obs> results = new ArrayList<>();
        Set<Attachment> attachments = new HashSet<>();

        for (Obs obs : encounter.getAllObs()) {
            processObservation(obs, order, results, attachments);
        }

        return new OrderObservations(results, attachments);
    }

    private void processObservation(Obs obs, Order order, List<Obs> results, Set<Attachment> attachments) {
        if (obs.getOrder() != null && obs.getOrder().equals(order)) {
            String conceptName = obs.getConcept().getName() != null ?
                    obs.getConcept().getName().getName() : "";

            if (LAB_REPORT_CONCEPT_NAME.equalsIgnoreCase(conceptName)) {
                if (obs.getValueText() != null) {
                    attachments.add(createAttachment(obs, order));
                }
            } else if (hasValue(obs)) {
                results.add(obs);
            }
        }

        // Recursively process group members to handle nested LAB_REPORT observations
        if (obs.hasGroupMembers()) {
            for (Obs member : obs.getGroupMembers()) {
                processObservation(member, order, results, attachments);
            }
        }
    }

    private Attachment createAttachment(Obs obs, Order order) {
        Attachment attachment = new Attachment();

        attachment.setContentUrl(obs.getValueText());
        attachment.setContentType(deriveContentType(obs.getValueText()));

        attachment.setTitle(getConceptFullySpecifiedName(order.getConcept()));

        attachment.setCreator(Context.getAuthenticatedUser());
        attachment.setDateCreated(new Date());
        attachment.setUuid(UUID.randomUUID().toString());
        attachment.setVoided(false);

        return attachment;
    }

    private String getConceptFullySpecifiedName(Concept concept) {
        if (concept == null) {
            return "Lab Report";
        }

        Locale locale = Context.getLocale();
        ConceptName fullySpecifiedName = concept.getFullySpecifiedName(locale);

        if (fullySpecifiedName != null) {
            return fullySpecifiedName.getName();
        }

        ConceptName preferredName = concept.getPreferredName(locale);
        if (preferredName != null) {
            return preferredName.getName();
        }

        return concept.getName() != null ? concept.getName().getName() : "Lab Report";
    }

    private boolean hasValue(Obs obs) {
        return obs.getValueNumeric() != null || obs.getValueText() != null || obs.getValueCoded() != null
                || obs.getValueComplex() != null || obs.getValueBoolean() != null;
    }

    private String deriveContentType(String url) {
        if (url == null) {
            return "application/octet-stream";
        }

        String lowerUrl = url.toLowerCase();
        if (lowerUrl.endsWith(".pdf")) {
            return "application/pdf";
        } else if (lowerUrl.endsWith(".jpg") || lowerUrl.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowerUrl.endsWith(".png")) {
            return "image/png";
        }

        return "application/octet-stream";
    }
}
