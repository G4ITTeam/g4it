/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apibatchevaluation.business;

import com.soprasteria.g4it.backend.apibatchevaluation.repository.InventoryEvaluationReportRepository;
import com.soprasteria.g4it.backend.apiindicator.business.IndicatorService;
import com.soprasteria.g4it.backend.apiinventory.business.InventoryService;
import com.soprasteria.g4it.backend.apiinventory.model.InventoryBO;
import com.soprasteria.g4it.backend.apiinventory.modeldb.InventoryEvaluationReport;
import com.soprasteria.g4it.backend.common.utils.EvaluationBatchStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Inventory Evaluation service.
 */
@Service
public class InventoryEvaluationService {

    @Autowired
    InventoryEvaluationReportRepository inventoryEvaluationReportRepository;
    /**
     * Inventory Evaluation Job Service.
     */
    @Autowired
    private InventoryEvaluationJobService inventoryEvaluationJobService;
    /**
     * Inventory Service.
     */
    @Autowired
    private InventoryService inventoryService;
    /**
     * Indicator Service.
     */
    @Autowired
    private IndicatorService indicatorService;

    private InventoryEvaluationReport inventoryEvaluationReport;

    /**
     * Launch loading batch job.
     *
     * @param subscriber   the subscriber.
     * @param organization the organization.
     * @param inventoryId  the inventory id.
     * @return spring batch job instance id.
     */
    public Long launchEvaluationBatchJob(final String subscriber, final String organization, final Long inventoryId) {
        final InventoryBO inventory = inventoryService.getInventory(inventoryId);
        // Remove last indicators if present.
        inventoryService.getLastBatchName(inventory).ifPresent(batchName -> indicatorService.deleteIndicators(organization, batchName));
        // Launch evaluation.
        return inventoryEvaluationJobService.launchInventoryEvaluation(organization, inventory.getName(), inventoryId);
    }

    /**
     * Delete evaluation batch job.
     *
     * @param organization the organization.
     * @param inventoryId  the inventory id.
     */
    public void deleteEvaluationBatchJob(final String organization, final Long inventoryId) {
        inventoryEvaluationJobService.deleteJobInstances(organization, inventoryId);
    }


    /**
     * Update batch status
     *
     * @param batchName   batch name
     * @param batchStatus batch status
     */
    public void updateBatchStatus(String batchName, EvaluationBatchStatus batchStatus) {
        InventoryEvaluationReport evaluationReport = inventoryEvaluationReportRepository.findByBatchName(batchName);
        evaluationReport.setBatchStatusCode(batchStatus.name());
        inventoryEvaluationReportRepository.save(evaluationReport);
    }
}
