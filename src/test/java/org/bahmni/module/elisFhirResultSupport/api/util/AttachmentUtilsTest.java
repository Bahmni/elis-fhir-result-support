package org.bahmni.module.elisFhirResultSupport.api.util;

import org.bahmni.module.elisFhirResultSupport.api.constants.ElisFhirResultSupportConstants;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptName;

import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AttachmentUtilsTest {

    @Test
    public void shouldInstantiateAttachmentUtils() {
        AttachmentUtils attachmentUtils = new AttachmentUtils();
        assertNotNull(attachmentUtils);
    }

    @Test
    public void shouldReturnFullySpecifiedNameWhenAvailable() {
        Concept concept = mock(Concept.class);
        ConceptName fullySpecifiedName = mock(ConceptName.class);
        Locale locale = Locale.ENGLISH;

        when(concept.getFullySpecifiedName(locale)).thenReturn(fullySpecifiedName);
        when(fullySpecifiedName.getName()).thenReturn("Blood Test");

        String result = AttachmentUtils.getConceptFullySpecifiedName(concept, locale);

        assertEquals("Blood Test", result);
    }

    @Test
    public void shouldReturnPreferredNameWhenFullySpecifiedNameIsNull() {
        Concept concept = mock(Concept.class);
        ConceptName preferredName = mock(ConceptName.class);
        Locale locale = Locale.ENGLISH;

        when(concept.getFullySpecifiedName(locale)).thenReturn(null);
        when(concept.getPreferredName(locale)).thenReturn(preferredName);
        when(preferredName.getName()).thenReturn("Blood Test Preferred");

        String result = AttachmentUtils.getConceptFullySpecifiedName(concept, locale);

        assertEquals("Blood Test Preferred", result);
    }

    @Test
    public void shouldReturnNameWhenFullySpecifiedAndPreferredNamesAreNull() {
        Concept concept = mock(Concept.class);
        ConceptName name = mock(ConceptName.class);
        Locale locale = Locale.ENGLISH;

        when(concept.getFullySpecifiedName(locale)).thenReturn(null);
        when(concept.getPreferredName(locale)).thenReturn(null);
        when(concept.getName(locale)).thenReturn(name);
        when(name.getName()).thenReturn("Blood Test Name");

        String result = AttachmentUtils.getConceptFullySpecifiedName(concept, locale);

        assertEquals("Blood Test Name", result);
    }

    @Test
    public void shouldReturnDefaultTitleWhenConceptIsNull() {
        String result = AttachmentUtils.getConceptFullySpecifiedName(null, Locale.ENGLISH);

        assertEquals(ElisFhirResultSupportConstants.DEFAULT_ATTACHMENT_TITLE, result);
    }

    @Test
    public void shouldReturnDefaultTitleWhenAllNamesAreNull() {
        Concept concept = mock(Concept.class);
        Locale locale = Locale.ENGLISH;

        when(concept.getFullySpecifiedName(locale)).thenReturn(null);
        when(concept.getPreferredName(locale)).thenReturn(null);
        when(concept.getName(locale)).thenReturn(null);

        String result = AttachmentUtils.getConceptFullySpecifiedName(concept, locale);

        assertEquals(ElisFhirResultSupportConstants.DEFAULT_ATTACHMENT_TITLE, result);
    }

    @Test
    public void shouldReturnPdfContentTypeForPdfFile() {
        String result = AttachmentUtils.deriveContentType("report.pdf");

        assertEquals("application/pdf", result);
    }

    @Test
    public void shouldReturnJpegContentTypeForJpegFile() {
        String result = AttachmentUtils.deriveContentType("image.jpeg");

        assertEquals("image/jpeg", result);
    }

    @Test
    public void shouldReturnJpegContentTypeForJpgFile() {
        String result = AttachmentUtils.deriveContentType("image.jpg");

        assertEquals("image/jpeg", result);
    }

    @Test
    public void shouldReturnPngContentTypeForPngFile() {
        String result = AttachmentUtils.deriveContentType("image.png");

        assertEquals("image/png", result);
    }

    @Test
    public void shouldReturnDefaultContentTypeForUnknownExtension() {
        String result = AttachmentUtils.deriveContentType("document.xyz");

        assertEquals(ElisFhirResultSupportConstants.DEFAULT_CONTENT_TYPE, result);
    }

    @Test
    public void shouldReturnDefaultContentTypeForFileWithoutExtension() {
        String result = AttachmentUtils.deriveContentType("document");

        assertEquals(ElisFhirResultSupportConstants.DEFAULT_CONTENT_TYPE, result);
    }

    @Test
    public void shouldHandleUrlWithPath() {
        String result = AttachmentUtils.deriveContentType("http://example.com/reports/lab-result.pdf");

        assertEquals("application/pdf", result);
    }
}
