/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apibatchexport.business;

import com.soprasteria.g4it.backend.apibatchexport.exception.EvaluationNotFoundException;
import com.soprasteria.g4it.backend.apiinventory.business.InventoryService;
import com.soprasteria.g4it.backend.apiinventory.model.InventoryBO;
import com.soprasteria.g4it.backend.apiinventory.model.InventoryEvaluationReportBO;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;

/**
 * Inventory Service.
 */
@Service
public class InventoryExportService {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private ExportJobService exportJobService;

    /**
     * Create an export request for the inventory
     *
     * @param subscriber   the subscriber.
     * @param organization Organization
     * @param inventoryId  Inventory id
     * @param username     the username who made the export request.
     */
    public void createExportRequest(final String subscriber,
                                    final String organization,
                                    final Long inventoryId,
                                    final String username) {

        final InventoryBO inventory = inventoryService.getInventory(inventoryId);
        if (CollectionUtils.isEmpty(inventory.getEvaluationReports())
                || inventory.getEvaluationReports().stream().noneMatch(report -> "COMPLETED".equals(report.getBatchStatusCode()))) {
            throw new EvaluationNotFoundException();
        }

        exportJobService.launchExport(subscriber, organization, inventory.getId(), inventory.getName(),
                inventory.getEvaluationReports()
                        .stream()
                        .max(Comparator.comparing(InventoryEvaluationReportBO::getEndTime))
                        .orElseThrow()
                        .getBatchName(),
                username);
    }

    /**
     * Delete export batch job.
     *
     * @param inventoryId the inventory id.
     */
    public void deleteExportBatchJob(final Long inventoryId) {
        exportJobService.deleteJobInstances(inventoryId);
    }

}
