package org.bahmni.module.elisFhirResultSupport.api.domain;

import org.bahmni.module.fhir2AddlExtension.api.model.Attachment;
import org.openmrs.Obs;

import java.util.List;
import java.util.Set;


public class OrderObservations {
	
	private final List<Obs> results;
	
	private final Set<Attachment> attachments;
	
	public OrderObservations(List<Obs> results, Set<Attachment> attachments) {
		this.results = results;
		this.attachments = attachments;
	}
	
	public List<Obs> getResults() {
		return results;
	}
	
	public Set<Attachment> getAttachments() {
		return attachments;
	}
}
