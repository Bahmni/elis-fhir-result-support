package org.bahmni.module.elisFhirResultSupport.api.util;

import org.openmrs.Obs;

public class ObsUtils {

	public static boolean hasValue(Obs obs) {
		return obs.getValueNumeric() != null || obs.getValueText() != null || obs.getValueCoded() != null
		        || obs.getValueComplex() != null || obs.getValueBoolean() != null || obs.getValueDatetime() != null;
	}
}
