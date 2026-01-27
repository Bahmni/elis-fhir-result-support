package org.bahmni.module.elisFhirResultSupport.api.util;

import org.bahmni.module.elisFhirResultSupport.api.constants.ElisFhirResultSupportConstants;
import org.openmrs.Concept;
import org.openmrs.ConceptName;

import java.net.URLConnection;
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
		
		return concept.getName(locale) != null ? concept.getName(locale).getName()
		        : ElisFhirResultSupportConstants.DEFAULT_ATTACHMENT_TITLE;
	}
	
	public static String deriveContentType(String url) {
		String contentType = URLConnection.guessContentTypeFromName(url);
		return contentType != null ? contentType : ElisFhirResultSupportConstants.DEFAULT_CONTENT_TYPE;
	}
}
