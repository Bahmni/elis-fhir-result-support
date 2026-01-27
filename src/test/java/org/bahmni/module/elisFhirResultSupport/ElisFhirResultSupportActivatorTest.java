package org.bahmni.module.elisFhirResultSupport;

import org.apache.commons.logging.Log;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ElisFhirResultSupportActivatorTest {

    private ElisFhirResultSupportActivator activator;

    @Mock
    private Log log;

    @Before
    public void setUp() throws Exception {
        activator = new ElisFhirResultSupportActivator();
        Field logField = ElisFhirResultSupportActivator.class.getDeclaredField("log");
        logField.setAccessible(true);
        logField.set(activator, log);
    }

    @Test
    public void shouldLogMessageWhenModuleStarted() {
        activator.started();
        verify(log).info("Started ELIS FHIR Result Support Module");
    }

    @Test
    public void shouldLogMessageWhenModuleStopped() {
        activator.stopped();
        verify(log).info("Stopped ELIS FHIR Result Support Module");
    }
}
