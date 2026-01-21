package org.bahmni.module.elisFhirResultSupport.api.helper;

import org.bahmni.module.elisFhirResultSupport.api.domain.OrderObservations;
import org.bahmni.module.fhir2AddlExtension.api.model.Attachment;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.api.ConceptService;
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
	
	private final ConceptService conceptService;
	
	public ObservationExtractor(ConceptService conceptService) {
		this.conceptService = conceptService;
	}
	
	public OrderObservations extractObservationsAndAttachments(Order order, Encounter encounter) {
		List<Obs> results = new ArrayList<>();
		Set<Attachment> attachments = new HashSet<>();
		Concept labReportConcept = conceptService.getConceptByName(LAB_REPORT_CONCEPT_NAME);
		
		for (Obs obs : encounter.getAllObs()) {
			if (obs.getOrder() != null && obs.getOrder().equals(order)) {
				if (obs.getConcept().equals(labReportConcept)) {
					if (obs.getValueText() != null) {
						attachments.add(createAttachment(obs, order));
					}
				} else if (hasValue(obs)) {
					results.add(obs);
				}
			}
		}
		
		return new OrderObservations(results, attachments);
	}
	
	private Attachment createAttachment(Obs obs, Order order) {
		Attachment attachment = new Attachment();
		
		attachment.setContentUrl(obs.getValueText());
		attachment.setContentType(deriveContentType(obs.getValueText()));
		
		attachment.setTitle(getConceptFullySpecifiedName(order.getConcept()));//TODO - TBD if FSN is needed
		
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
		
		// Fallback to preferred name if FSN not available
		ConceptName preferredName = concept.getPreferredName(locale);
		if (preferredName != null) {
			return preferredName.getName();
		}
		
		// Last resort fallback
		return concept.getName() != null ? concept.getName().getName() : "Lab Report";
	}
	
	private boolean hasValue(Obs obs) {
		// TODO : Check if obs has any value set
		// Note: We check value_text here for non-LAB_REPORT observations that may have text values
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
