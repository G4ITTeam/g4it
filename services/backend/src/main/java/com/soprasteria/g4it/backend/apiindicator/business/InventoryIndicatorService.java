/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiindicator.business;

import com.soprasteria.g4it.backend.apiindicator.model.*;
import com.soprasteria.g4it.backend.apiinventory.business.InventoryService;
import com.soprasteria.g4it.backend.apiinventory.model.InventoryBO;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Inventory Service.
 */
@Service
@NoArgsConstructor
public class InventoryIndicatorService {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private IndicatorService indicatorService;

    @Autowired
    private FilterService filterService;

    /**
     * Get equipment indicator filters.
     *
     * @param subscriber   the subscriber.
     * @param organization the organization.
     * @param inventoryId  the inventory id.
     * @return filters.
     */
    public EquipmentFiltersBO getEquipmentFilters(final String subscriber, final String organization, final Long inventoryId) {
        final InventoryBO inventory = inventoryService.getInventory(subscriber, organization, inventoryId);
        return filterService.getEquipmentFilters(organization, inventory.getId(), getLastBatchName(inventory));
    }

    /**
     * Get application indicator filters.
     *
     * @param subscriber   the subscriber.
     * @param organization the organization.
     * @param inventoryId  the inventory id.
     * @return filters.
     */
    public ApplicationFiltersBO getApplicationFilters(final String subscriber, final String organization, final Long inventoryId,
                                                      final String domain, final String subDomain, final String applicationName) {

        final InventoryBO inventory = inventoryService.getInventory(subscriber, organization, inventoryId);
        return filterService.getApplicationFilters(inventory.getId(), getLastBatchName(inventory), domain, subDomain, applicationName);
    }

    /**
     * Get inventory indicators.
     * *
     * * @param subscriber    the subscriber.
     * * @param organization  the organization.
     * * @param inventoryId the inventory id.
     * * @return indicators.
     */
    public Map<String, EquipmentIndicatorBO> getEquipmentIndicators(final String subscriber, final String organization, final Long inventoryId) {
        final InventoryBO inventory = inventoryService.getInventory(subscriber, organization, inventoryId);
        return indicatorService.getEquipmentIndicators(organization, getLastBatchName(inventory));
    }

    /**
     * Get inventory application indicators.
     *
     * @param subscriber   the subscriber.
     * @param organization the organization.
     * @param inventoryId  the inventory id.
     * @return indicators.
     */

    public List<ApplicationIndicatorBO<ApplicationImpactBO>> getApplicationIndicators(final String subscriber, final String organization, final Long inventoryId) {
        final InventoryBO inventory = inventoryService.getInventory(subscriber, organization, inventoryId);
        return indicatorService.getApplicationIndicators(organization, getLastBatchName(inventory), inventory.getId());
    }

    /**
     * Get inventory application VM indicators.
     *
     * @param subscriber      the subscriber.
     * @param organization    the organization.
     * @param inventoryId     the inventory id.
     * @param applicationName the application name.
     * @param criteria        the criteria.
     * @return indicators.
     */

    public List<ApplicationIndicatorBO<ApplicationVmImpactBO>> getApplicationVmIndicators(final String subscriber, final String organization,
                                                                                          final Long inventoryId,
                                                                                          final String applicationName,
                                                                                          final String criteria) {
        final InventoryBO inventory = inventoryService.getInventory(subscriber, organization, inventoryId);
        return indicatorService.getApplicationVmIndicators(organization, getLastBatchName(inventory), inventory.getId(), applicationName, criteria);
    }

    /**
     * Delete inventory indicators.
     *
     * @param subscriber   the subscriber.
     * @param organization the organization.
     * @param inventoryId  the inventory id.
     */
    public void deleteIndicators(final String subscriber, final String organization, final Long inventoryId) {
        inventoryService.getInventory(subscriber, organization, inventoryId).getEvaluationReports().forEach(report ->
                indicatorService.deleteIndicators(organization, report.getBatchName()));
    }

    /**
     * Get datacenter indicators.
     *
     * @param subscriber   the subscriber.
     * @param organization the organization.
     * @param inventoryId  the inventory id.
     * @return datacenter indicators
     */
    public List<DataCentersInformationBO> getDataCenterIndicators(final String subscriber, final String organization, final Long inventoryId) {
        return indicatorService.getDataCenterIndicators(subscriber, organization, inventoryId);
    }

    /**
     * Get physical equipment average age indicators.
     *
     * @param subscriber   the subscriber.
     * @param organization the organization.
     * @param inventoryId  the inventory id.
     * @return datacenter indicators
     */
    public List<PhysicalEquipmentsAvgAgeBO> getPhysicalEquipmentAvgAge(final String subscriber, final String organization, final Long inventoryId) {
        return indicatorService.getPhysicalEquipmentAvgAge(subscriber, organization, inventoryId);
    }

    /**
     * Get physical equipment low impact indicators.
     *
     * @param subscriber   the subscriber.
     * @param organization the organization.
     * @param inventoryId  the inventory id.
     * @return datacenter indicators
     */
    public List<PhysicalEquipmentLowImpactBO> getPhysicalEquipmentsLowImpact(final String subscriber, final String organization, final Long inventoryId) {
        return indicatorService.getPhysicalEquipmentsLowImpact(subscriber, organization, inventoryId);
    }

    /**
     * Get last batch name in the inventory business object.
     *
     * @param inventory the inventory business object.
     * @return the last batch name or else throw exception.
     */
    private String getLastBatchName(final InventoryBO inventory) {
        return inventoryService.getLastBatchName(inventory)
                .orElseThrow(() -> new G4itRestException("404", String.format("inventory %d has no batch executed", inventory.getId())));
    }
}
