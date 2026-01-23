package org.bahmni.module.elisFhirResultSupport.api.helper;

import org.bahmni.module.elisFhirResultSupport.api.constants.ElisFhirResultSupportConstants;
import org.bahmni.module.elisFhirResultSupport.api.domain.OrderObservations;
import org.bahmni.module.elisFhirResultSupport.api.util.AttachmentUtils;
import org.bahmni.module.elisFhirResultSupport.api.util.ObsUtils;
import org.bahmni.module.fhir2AddlExtension.api.model.Attachment;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.api.context.Context;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ObservationExtractor {

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

            if (ElisFhirResultSupportConstants.LAB_REPORT_CONCEPT_NAME.equalsIgnoreCase(conceptName)) {
                if (obs.getValueText() != null) {
                    attachments.add(createAttachment(obs, order));
                }
            } else if (ElisFhirResultSupportConstants.LAB_MAXNORMAL_CONCEPT_NAME.equalsIgnoreCase(conceptName) ||
                    ElisFhirResultSupportConstants.LAB_MINNORMAL_CONCEPT_NAME.equalsIgnoreCase(conceptName) || 
                    ElisFhirResultSupportConstants.LAB_NOTES_CONCEPT_NAME.equalsIgnoreCase(conceptName)) {
                return;
            } else if (ObsUtils.hasValue(obs)) {
                results.add(obs);
            }
        }

        if (obs.hasGroupMembers()) {
            for (Obs member : obs.getGroupMembers()) {
                processObservation(member, order, results, attachments);
            }
        }
    }

    private Attachment createAttachment(Obs obs, Order order) {
        Attachment attachment = new Attachment();

        attachment.setContentUrl(obs.getValueText());
        attachment.setContentType(AttachmentUtils.deriveContentType(obs.getValueText()));

        attachment.setTitle(AttachmentUtils.getConceptFullySpecifiedName(order.getConcept(), Context.getLocale()));

        attachment.setCreator(Context.getAuthenticatedUser());
        attachment.setDateCreated(new Date());
        attachment.setUuid(UUID.randomUUID().toString());
        attachment.setVoided(false);

        return attachment;
    }
}
