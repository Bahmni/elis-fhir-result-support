package org.bahmni.module.elisFhirResultSupport.api.service;

import org.bahmni.module.fhir2AddlExtension.api.model.FhirDiagnosticReportExt;
import org.openmrs.Encounter;
import org.openmrs.Order;

public interface ElisFhirDiagnosticReportService {
	
	FhirDiagnosticReportExt createOrUpdateDiagnosticReport(Order order, Encounter encounter);
	
	FhirDiagnosticReportExt findExistingReport(Encounter encounter, Order order);
}
