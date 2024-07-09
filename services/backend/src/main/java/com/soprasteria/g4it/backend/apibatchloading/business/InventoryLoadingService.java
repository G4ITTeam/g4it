/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apibatchloading.business;

import com.soprasteria.g4it.backend.apibatchloading.model.InventoryLoadingSession;
import com.soprasteria.g4it.backend.apiinventory.business.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Inventory Loading service.
 */
@Service
public class InventoryLoadingService {

    /**
     * Inventory Loading Job Service.
     */
    @Autowired
    private InventoryLoadingJobService inventoryLoadingJobService;

    /**
     * Inventory Service.
     */
    @Autowired
    private InventoryService inventoryService;

    /**
     * Launch loading batch job.
     *
     * @param session the current session to launch.
     * @return spring batch job instance id.
     */
    public Long launchLoadingBatchJob(final InventoryLoadingSession session) {
        final String inventoryName = inventoryService.getInventory(session.getSubscriber(), session.getOrganizationId(), session.getInventoryId()).getName();
        session.setInventoryName(inventoryName);
        return inventoryLoadingJobService.launchInventoryIntegration(session);
    }


    /**
     * Delete loading batch job.
     *
     * @param organizationId the organization's id.
     * @param inventoryId    the inventory id.
     */
    public void deleteLoadingBatchJob(final Long organizationId, final Long inventoryId) {
        inventoryLoadingJobService.deleteJobInstances(organizationId, inventoryId);
    }
}
