package org.bahmni.module.elisFhirResultSupport.api.util;

import org.bahmni.module.elisFhirResultSupport.api.constants.ElisFhirResultSupportConstants;
import org.openmrs.Concept;
import org.openmrs.ConceptName;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AttachmentUtils {

	private static final Map<String, String> EXTENSION_TO_MIME_TYPE = new HashMap<>();

	static {
		EXTENSION_TO_MIME_TYPE.put("pdf", "application/pdf");
		EXTENSION_TO_MIME_TYPE.put("doc", "application/msword");
		EXTENSION_TO_MIME_TYPE.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
		EXTENSION_TO_MIME_TYPE.put("jpg", "image/jpeg");
		EXTENSION_TO_MIME_TYPE.put("jpeg", "image/jpeg");
		EXTENSION_TO_MIME_TYPE.put("png", "image/png");
	}

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
		String extension = extractFileExtension(url);
		if (extension != null && EXTENSION_TO_MIME_TYPE.containsKey(extension)) {
			return EXTENSION_TO_MIME_TYPE.get(extension);
		}

		return ElisFhirResultSupportConstants.DEFAULT_CONTENT_TYPE;
	}

    // TODO: Upon upgrading to Java 11 or higher, we can leverag
    //  URLConnection.guessContentTypeFromName to determine
    //  an object's content type based on the URL's "file" component.
    //  Currently, doc and docx formats are not natively supported therefore,
    //  this custom extraction logic has been implemented as a workaround.
	private static String extractFileExtension(String url) {
		int lastDotIndex = url.lastIndexOf('.');
		int lastSlashIndex = url.lastIndexOf('/');
		int queryIndex = url.indexOf('?');

		if (lastDotIndex == -1 || lastDotIndex < lastSlashIndex) {
			return null;
		}

		int endIndex = queryIndex != -1 ? queryIndex : url.length();
		if (lastDotIndex >= endIndex) {
			return null;
		}

		return url.substring(lastDotIndex + 1, endIndex).toLowerCase();
	}
}
