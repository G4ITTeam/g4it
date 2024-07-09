/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiinventory.business;

import com.soprasteria.g4it.backend.apibatchevaluation.business.InventoryEvaluationService;
import com.soprasteria.g4it.backend.apibatchexport.business.InventoryExportService;
import com.soprasteria.g4it.backend.apibatchloading.business.InventoryLoadingService;
import com.soprasteria.g4it.backend.apiindicator.business.InventoryIndicatorService;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.apiinventory.repository.InventoryRepository;
import com.soprasteria.g4it.backend.apiuser.business.OrganizationService;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@NoArgsConstructor
public class InventoryDeleteService {

    /**
     * Repository to access inventory data.
     */
    @Autowired
    private InventoryRepository inventoryRepository;

    /**
     * The organization service.
     */
    @Autowired
    private OrganizationService organizationService;

    /**
     * Inventory Evaluation service.
     */
    @Autowired
    private InventoryEvaluationService inventoryEvaluationService;

    /**
     * Inventory Loading service.
     */
    @Autowired
    private InventoryLoadingService inventoryLoadingService;

    /**
     * Inventory Export service.
     */
    @Autowired
    private InventoryExportService inventoryExportService;

    /**
     * Inventory Indicator Service
     */
    @Autowired
    private InventoryIndicatorService inventoryIndicatorService;


    /**
     * Delete all inventories in an organization.
     *
     * @param subscriberName the client subscriber name.
     * @param organizationId the linked organization's id.
     */
    public void deleteInventories(final String subscriberName, final Long organizationId) {
        final Organization linkedOrganization = organizationService.getOrganizationById(organizationId);
        inventoryRepository.findByOrganization(linkedOrganization)
                .forEach(inventory -> deleteInventory(subscriberName, organizationId, inventory));
    }


    /**
     * Delete an inventory for an organization on a date.
     *
     * @param subscriberName the client subscriber name.
     * @param organizationId the organization id.
     * @param inventoryId    the inventory id.
     */
    public void deleteInventory(final String subscriberName, final Long organizationId, final Long inventoryId) {
        final Organization linkedOrganization = organizationService.getOrganizationById(organizationId);
        inventoryRepository.findByOrganizationAndId(linkedOrganization, inventoryId)
                .ifPresent(inventory -> deleteInventory(subscriberName, organizationId, inventory));
    }


    /**
     * Delete the inventory based on the inventory database object
     *
     * @param subscriberName the client subscriber name.
     * @param organizationId the organization's id.
     * @param inventory      the inventory database object.
     */

    public void deleteInventory(final String subscriberName, final Long organizationId, final Inventory inventory) {
        Long inventoryId = inventory.getId();
        inventoryIndicatorService.deleteIndicators(subscriberName, organizationId, inventoryId);

        // Remove batch job instance (all data linked to the repository to delete).
        inventoryEvaluationService.deleteEvaluationBatchJob(organizationId, inventoryId);
        inventoryLoadingService.deleteLoadingBatchJob(organizationId, inventoryId);
        inventoryExportService.deleteExportBatchJob(inventoryId);

        // Remove inventory.
        inventoryRepository.deleteByInventoryId(inventory.getId());
    }

}
