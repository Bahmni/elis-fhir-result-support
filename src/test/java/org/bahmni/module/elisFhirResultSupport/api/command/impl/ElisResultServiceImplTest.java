package org.bahmni.module.elisFhirResultSupport.api.command.impl;

import org.bahmni.module.elisFhirResultSupport.api.service.ElisFhirDiagnosticReportService;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.api.OrderService;

import java.util.*;

import static org.mockito.Mockito.*;

public class ElisResultServiceImplTest {

    private ElisResultServiceImpl service;
    private OrderService orderService;
    private ElisFhirDiagnosticReportService diagnosticReportService;

    @Before
    public void setUp() {
        orderService = mock(OrderService.class);
        diagnosticReportService = mock(ElisFhirDiagnosticReportService.class);
        service = new ElisResultServiceImpl(orderService, diagnosticReportService);
    }

    @Test
    public void shouldProcessEncountersAndUpdateOrderStatus() {
        Encounter encounter = mock(Encounter.class);
        Order order = mock(Order.class);
        Obs obsWithOrder = mock(Obs.class);
        Obs obsWithoutOrder = mock(Obs.class);

        when(obsWithOrder.getOrder()).thenReturn(order);
        when(obsWithoutOrder.getOrder()).thenReturn(null);
        when(encounter.getAllObs()).thenReturn(new HashSet<>(Arrays.asList(obsWithOrder, obsWithoutOrder)));
        when(order.getFulfillerStatus()).thenReturn(null);

        service.onResult(Collections.singletonList(encounter));

        verify(orderService).updateOrderFulfillerStatus(eq(order), eq(Order.FulfillerStatus.IN_PROGRESS), isNull());
        verify(diagnosticReportService).createOrUpdateDiagnosticReport(order, encounter);
        verify(diagnosticReportService, times(1)).createOrUpdateDiagnosticReport(any(), any());
    }

    @Test
    public void shouldSkipOrderUpdateWhenStatusIsAlreadyInProgress() {
        Encounter encounter = mock(Encounter.class);
        Order order = mock(Order.class);
        Obs obs = mock(Obs.class);

        when(obs.getOrder()).thenReturn(order);
        when(encounter.getAllObs()).thenReturn(Collections.singleton(obs));
        when(order.getFulfillerStatus()).thenReturn(Order.FulfillerStatus.IN_PROGRESS);

        service.onResult(Collections.singletonList(encounter));

        verify(orderService, never()).updateOrderFulfillerStatus(any(), any(), any());
        verify(diagnosticReportService).createOrUpdateDiagnosticReport(order, encounter);
    }

    @Test
    public void shouldContinueProcessingOtherEncountersWhenOneEncounterFails() {
        Encounter encounter1 = mock(Encounter.class);
        Encounter encounter2 = mock(Encounter.class);
        Order order = mock(Order.class);
        Obs obs = mock(Obs.class);

        when(encounter1.getAllObs()).thenThrow(new RuntimeException("Test exception"));
        when(encounter2.getAllObs()).thenReturn(Collections.singleton(obs));
        when(obs.getOrder()).thenReturn(order);
        when(order.getFulfillerStatus()).thenReturn(null);

        service.onResult(Arrays.asList(encounter1, encounter2));

        verify(orderService).updateOrderFulfillerStatus(eq(order), eq(Order.FulfillerStatus.IN_PROGRESS), isNull());
        verify(diagnosticReportService).createOrUpdateDiagnosticReport(order, encounter2);
    }
}
