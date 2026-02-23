package org.bahmni.module.elisFhirResultSupport.api.command.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.module.elisatomfeedclient.api.command.ELISResultPostSaveCommand;
import org.bahmni.module.elisFhirResultSupport.api.service.ElisFhirDiagnosticReportService;
import org.bahmni.module.fhir2AddlExtension.api.model.FhirDiagnosticReportExt;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.api.OrderService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ElisResultServiceImpl implements ELISResultPostSaveCommand {

    private static final Logger logger = LogManager.getLogger(ElisResultServiceImpl.class);

    private final OrderService orderService;
    private final ElisFhirDiagnosticReportService diagnosticReportService;

    public ElisResultServiceImpl(OrderService orderService, ElisFhirDiagnosticReportService diagnosticReportService) {
        this.orderService = orderService;
        this.diagnosticReportService = diagnosticReportService;
    }

    @Override
    public void onResult(List<Encounter> encounters) {
        logger.info("Processing {} lab result encounters", encounters.size());

        for (Encounter encounter : encounters) {
            try {
                processEncounter(encounter);
            } catch (Exception e) {
                logger.error("Error processing encounter {}: {}", encounter.getUuid(), e.getMessage(), e);
            }
        }

        logger.info("Completed processing {} encounters", encounters.size());
    }

    private void processEncounter(Encounter encounter) {
        logger.info("Processing encounter: {}", encounter.getUuid());

        Set<Order> orders = extractOrdersFromEncounter(encounter);

        for (Order order : orders) {
            FhirDiagnosticReportExt savedReport = diagnosticReportService.createOrUpdateDiagnosticReport(order, encounter);
            updateOrderFulfillerStatus(order, mapReportStatusToFulfillerStatus(savedReport));
        }
    }

    private Set<Order> extractOrdersFromEncounter(Encounter encounter) {
        Set<Order> uniqueOrders = new HashSet<>();

        for (Obs obs : encounter.getAllObs()) {
            if (obs.getOrder() != null) {
                uniqueOrders.add(obs.getOrder());
            }
        }

        return uniqueOrders;
    }

    private void updateOrderFulfillerStatus(Order order, Order.FulfillerStatus fulfillerStatus) {

        if (fulfillerStatus.equals(order.getFulfillerStatus())) {
            logger.info("Order {} already has {} status, skipping update", order.getUuid(), fulfillerStatus);
            return;
        }

        orderService.updateOrderFulfillerStatus(order, fulfillerStatus, null);
        logger.info("Updated order {} status to {}", order.getUuid(), fulfillerStatus);
    }

    private Order.FulfillerStatus mapReportStatusToFulfillerStatus(FhirDiagnosticReportExt fhirDiagnosticReportExt) {
        if (fhirDiagnosticReportExt == null) {
            return null;
        }
        if (fhirDiagnosticReportExt.getStatusExt().equals(FhirDiagnosticReportExt.DiagnosticReportStatusExt.FINAL))
            return Order.FulfillerStatus.COMPLETED;
        return Order.FulfillerStatus.IN_PROGRESS;
    }
}
