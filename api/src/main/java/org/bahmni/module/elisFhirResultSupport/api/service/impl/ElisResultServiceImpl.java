package org.bahmni.module.elisFhirResultSupport.api.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.module.elisatomfeedclient.api.command.ELISResultPostSaveCommand;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.api.OrderService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ElisResultServiceImpl implements ELISResultPostSaveCommand {
    
    private static final Logger logger = LogManager.getLogger(ElisResultServiceImpl.class);

    private final OrderService orderService;
    
    public ElisResultServiceImpl(OrderService orderService) {
        this.orderService = orderService;
    }
    
    @Override
    public void onResult(List<Encounter> encounters) {
        logger.info("Processing {} lab result encounters", encounters.size());
        
        for (Encounter encounter : encounters) {
            try {
                processEncounter(encounter);
            } catch (Exception e) {
                logger.error("Error processing encounter {}: {}", 
                    encounter.getUuid(), e.getMessage(), e);
            }
        }
        
        logger.info("Completed processing {} encounters", encounters.size());
    }
    
    private void processEncounter(Encounter encounter) {
        logger.info("Processing encounter: {}", encounter.getUuid());
        
        Set<Order> orders = extractOrdersFromEncounter(encounter);
        
        for (Order order : orders) {
            updateOrderFulfillerStatus(order);
            List<Obs> observations = getObservationsByOrder(order, encounter);
            createFhirDiagnosticReport(order, observations, encounter);
        }
    }
    
    private Set<Order> extractOrdersFromEncounter(Encounter encounter) {
        Set<Order> uniqueOrders = new HashSet<>();

        //TODO ; Verify an episode can have multiple orders
        
        for (Obs obs : encounter.getAllObs()) {
            if (obs.getOrder() != null) {
                uniqueOrders.add(obs.getOrder());
            }
        }
        
        return uniqueOrders;
    }
    
    private void updateOrderFulfillerStatus(Order order) {
        order.setFulfillerStatus(Order.FulfillerStatus.IN_PROGRESS);
        orderService.saveOrder(order, null);
        logger.info("Updated order {} status to IN_PROGRESS", order.getUuid());
    }
    
    private List<Obs> getObservationsByOrder(Order order, Encounter encounter) {
        logger.info("Getting observations for order: {}", order.getUuid());
        return new ArrayList<>();
    }
    
    private void createFhirDiagnosticReport(Order order, List<Obs> observations, Encounter encounter) {
        logger.info("Creating FHIR diagnostic report for order: {}", order.getUuid());
    }
}
