package org.bahmni.module.elisFhirResultSupport.api.util;

import org.bahmni.module.elisFhirResultSupport.api.constants.ElisFhirResultSupportConstants;
import org.openmrs.Concept;
import org.openmrs.ConceptName;

import java.util.Locale;

public class AttachmentUtils {
	
	public static String getConceptFullySpecifiedName(Concept concept, Locale locale) {
		if (concept == null) {
			return ElisFhirResultSupportConstants.DEFAULT_ATTACHMENT_TITLE;
		}
		
		ConceptName fullySpecifiedName = concept.getFullySpecifiedName(locale);
		if (fullySpecifiedName != null) {
			return fullySpecifiedName.getName();
		}
		
		ConceptName preferredName = concept.getPreferredName(locale);
		if (preferredName != null) {
			return preferredName.getName();
		}
		
		return concept.getName() != null ? concept.getName().getName()
		        : ElisFhirResultSupportConstants.DEFAULT_ATTACHMENT_TITLE;
	}
	
	public static String deriveContentType(String url) {
		if (url == null) {
			return null;
		}
		
		String attachmentUrl = url.toLowerCase();
		if (attachmentUrl.endsWith(ElisFhirResultSupportConstants.EXTENSION_PDF)) {
			return ElisFhirResultSupportConstants.CONTENT_TYPE_PDF;
		} else if (attachmentUrl.endsWith(ElisFhirResultSupportConstants.EXTENSION_JPG)
		        || attachmentUrl.endsWith(ElisFhirResultSupportConstants.EXTENSION_JPEG)) {
			return ElisFhirResultSupportConstants.CONTENT_TYPE_JPEG;
		} else if (attachmentUrl.endsWith(ElisFhirResultSupportConstants.EXTENSION_PNG)) {
			return ElisFhirResultSupportConstants.CONTENT_TYPE_PNG;
		}
		
		return ElisFhirResultSupportConstants.DEFAULT_CONTENT_TYPE;
	}
}
