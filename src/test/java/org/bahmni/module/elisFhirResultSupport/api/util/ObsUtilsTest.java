package org.bahmni.module.elisFhirResultSupport.api.util;

import org.junit.Test;
import org.openmrs.Obs;

import java.util.Date;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ObsUtilsTest {

    @Test
    public void shouldInstantiateObsUtils() {
        ObsUtils obsUtils = new ObsUtils();
        assertNotNull(obsUtils);
    }

    @Test
    public void shouldReturnTrueWhenObsHasValueNumeric() {
        Obs obs = mock(Obs.class);
        when(obs.getValueNumeric()).thenReturn(10.5);
        when(obs.getValueText()).thenReturn(null);
        when(obs.getValueCoded()).thenReturn(null);
        when(obs.getValueComplex()).thenReturn(null);
        when(obs.getValueBoolean()).thenReturn(null);
        when(obs.getValueDatetime()).thenReturn(null);

        assertTrue(ObsUtils.hasValue(obs));
    }

    @Test
    public void shouldReturnTrueWhenObsHasValueText() {
        Obs obs = mock(Obs.class);
        when(obs.getValueNumeric()).thenReturn(null);
        when(obs.getValueText()).thenReturn("test result");
        when(obs.getValueCoded()).thenReturn(null);
        when(obs.getValueComplex()).thenReturn(null);
        when(obs.getValueBoolean()).thenReturn(null);
        when(obs.getValueDatetime()).thenReturn(null);

        assertTrue(ObsUtils.hasValue(obs));
    }

    @Test
    public void shouldReturnTrueWhenObsHasValueCoded() {
        Obs obs = mock(Obs.class);
        when(obs.getValueNumeric()).thenReturn(null);
        when(obs.getValueText()).thenReturn(null);
        when(obs.getValueCoded()).thenReturn(mock(org.openmrs.Concept.class));
        when(obs.getValueComplex()).thenReturn(null);
        when(obs.getValueBoolean()).thenReturn(null);
        when(obs.getValueDatetime()).thenReturn(null);

        assertTrue(ObsUtils.hasValue(obs));
    }

    @Test
    public void shouldReturnTrueWhenObsHasValueComplex() {
        Obs obs = mock(Obs.class);
        when(obs.getValueNumeric()).thenReturn(null);
        when(obs.getValueText()).thenReturn(null);
        when(obs.getValueCoded()).thenReturn(null);
        when(obs.getValueComplex()).thenReturn("complex value");
        when(obs.getValueBoolean()).thenReturn(null);
        when(obs.getValueDatetime()).thenReturn(null);

        assertTrue(ObsUtils.hasValue(obs));
    }

    @Test
    public void shouldReturnTrueWhenObsHasValueBoolean() {
        Obs obs = mock(Obs.class);
        when(obs.getValueNumeric()).thenReturn(null);
        when(obs.getValueText()).thenReturn(null);
        when(obs.getValueCoded()).thenReturn(null);
        when(obs.getValueComplex()).thenReturn(null);
        when(obs.getValueBoolean()).thenReturn(true);
        when(obs.getValueDatetime()).thenReturn(null);

        assertTrue(ObsUtils.hasValue(obs));
    }

    @Test
    public void shouldReturnTrueWhenObsHasValueDatetime() {
        Obs obs = mock(Obs.class);
        when(obs.getValueNumeric()).thenReturn(null);
        when(obs.getValueText()).thenReturn(null);
        when(obs.getValueCoded()).thenReturn(null);
        when(obs.getValueComplex()).thenReturn(null);
        when(obs.getValueBoolean()).thenReturn(null);
        when(obs.getValueDatetime()).thenReturn(new Date());

        assertTrue(ObsUtils.hasValue(obs));
    }

    @Test
    public void shouldReturnFalseWhenObsHasNoValue() {
        Obs obs = mock(Obs.class);
        when(obs.getValueNumeric()).thenReturn(null);
        when(obs.getValueText()).thenReturn(null);
        when(obs.getValueCoded()).thenReturn(null);
        when(obs.getValueComplex()).thenReturn(null);
        when(obs.getValueBoolean()).thenReturn(null);
        when(obs.getValueDatetime()).thenReturn(null);

        assertFalse(ObsUtils.hasValue(obs));
    }

    @Test
    public void shouldReturnTrueWhenObsHasMultipleValues() {
        Obs obs = mock(Obs.class);
        when(obs.getValueNumeric()).thenReturn(5.0);
        when(obs.getValueText()).thenReturn("text");

        assertTrue(ObsUtils.hasValue(obs));
    }

    @Test
    public void shouldReturnTrueWhenValueBooleanIsFalse() {
        Obs obs = mock(Obs.class);
        when(obs.getValueNumeric()).thenReturn(null);
        when(obs.getValueText()).thenReturn(null);
        when(obs.getValueCoded()).thenReturn(null);
        when(obs.getValueComplex()).thenReturn(null);
        when(obs.getValueBoolean()).thenReturn(false);
        when(obs.getValueDatetime()).thenReturn(null);

        assertTrue(ObsUtils.hasValue(obs));
    }
}
