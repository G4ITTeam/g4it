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
import com.soprasteria.g4it.backend.apiuser.business.OrganizationService;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
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


    @Autowired
    private OrganizationService organizationService;


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

    /**
     * Get equipment indicator filters.
     *
     * @param subscriber     the subscriber.
     * @param organizationId the organization's id.
     * @param inventoryId    the inventory id.
     * @return filters.
     */
    public EquipmentFiltersBO getEquipmentFilters(final String subscriber, final Long organizationId, final Long inventoryId) {
        final InventoryBO inventory = inventoryService.getInventory(subscriber, organizationId, inventoryId);
        final Organization linkedOrganization = organizationService.getOrganizationById(organizationId);
        return filterService.getEquipmentFilters(subscriber, linkedOrganization, inventory.getId(), getLastBatchName(inventory));
    }

    /**
     * @param subscriber      The subscriber
     * @param organizationId  The organizationId
     * @param inventoryId     The inventoryId
     * @param domain          The domain
     * @param subDomain       The subDomain
     * @param applicationName The applicationName
     * @return ApplicationFiltersBO
     */
    public ApplicationFiltersBO getApplicationFilters(final String subscriber, final Long organizationId, final Long inventoryId,
                                                      final String domain, final String subDomain, final String applicationName) {

        final InventoryBO inventory = inventoryService.getInventory(subscriber, organizationId, inventoryId);
        return filterService.getApplicationFilters(subscriber, organizationId, inventory.getId(), getLastBatchName(inventory), domain, subDomain, applicationName);
    }

    /**
     * Get inventory indicators.
     * *
     * * @param subscriber    the subscriber.
     * * @param organizationId  the organizationId.
     * * @param inventoryId the inventory id.
     * * @return indicators.
     */
    public Map<String, EquipmentIndicatorBO> getEquipmentIndicators(final String subscriber, final Long organizationId, final Long inventoryId) {
        final InventoryBO inventory = inventoryService.getInventory(subscriber, organizationId, inventoryId);
        return indicatorService.getEquipmentIndicators(subscriber, organizationId, getLastBatchName(inventory));
    }

    /**
     * Get inventory application indicators.
     *
     * @param subscriber     the subscriber.
     * @param organizationId the organizationId.
     * @param inventoryId    the inventory id.
     * @return indicators.
     */

    public List<ApplicationIndicatorBO<ApplicationImpactBO>> getApplicationIndicators(final String subscriber, final Long organizationId, final Long inventoryId) {
        final InventoryBO inventory = inventoryService.getInventory(subscriber, organizationId, inventoryId);
        return indicatorService.getApplicationIndicators(subscriber, organizationId, getLastBatchName(inventory), inventory.getId());
    }

    /**
     * Get inventory application VM indicators.
     *
     * @param subscriber      the subscriber.
     * @param organizationId  the organizationId.
     * @param inventoryId     the inventory id.
     * @param applicationName the application name.
     * @param criteria        the criteria.
     * @return indicators.
     */

    public List<ApplicationIndicatorBO<ApplicationVmImpactBO>> getApplicationVmIndicators(final String subscriber, final Long organizationId,
                                                                                          final Long inventoryId,
                                                                                          final String applicationName,
                                                                                          final String criteria) {
        final InventoryBO inventory = inventoryService.getInventory(subscriber, organizationId, inventoryId);
        return indicatorService.getApplicationVmIndicators(subscriber, organizationId, getLastBatchName(inventory), inventory.getId(), applicationName, criteria);
    }

    /**
     * Delete inventory indicators.
     *
     * @param subscriber     the subscriber.
     * @param organizationId the organizationId.
     * @param inventoryId    the inventory id.
     */
    public void deleteIndicators(final String subscriber, final Long organizationId, final Long inventoryId) {
        inventoryService.getInventory(subscriber, organizationId, inventoryId).getEvaluationReports().forEach(report ->
                indicatorService.deleteIndicators(report.getBatchName()));
    }

    /**
     * Get datacenter indicators.
     *
     * @param subscriber     the subscriber.
     * @param organizationId the organizationId.
     * @param inventoryId    the inventory id.
     * @return datacenter indicators
     */
    public List<DataCentersInformationBO> getDataCenterIndicators(final String subscriber, final Long organizationId, final Long inventoryId) {
        return indicatorService.getDataCenterIndicators(subscriber, organizationId, inventoryId);
    }

    /**
     * Get physical equipment average age indicators.
     *
     * @param subscriber     the subscriber.
     * @param organizationId the organizationId.
     * @param inventoryId    the inventory id.
     * @return datacenter indicators
     */
    public List<PhysicalEquipmentsAvgAgeBO> getPhysicalEquipmentAvgAge(final String subscriber, final Long organizationId, final Long inventoryId) {
        return indicatorService.getPhysicalEquipmentAvgAge(subscriber, organizationId, inventoryId);
    }

    /**
     * Get physical equipment low impact indicators.
     *
     * @param subscriber     the subscriber.
     * @param organizationId the organizationId.
     * @param inventoryId    the inventory id.
     * @return datacenter indicators
     */
    public List<PhysicalEquipmentLowImpactBO> getPhysicalEquipmentsLowImpact(final String subscriber, final Long organizationId, final Long inventoryId) {
        return indicatorService.getPhysicalEquipmentsLowImpact(subscriber, organizationId, inventoryId);
    }
}
