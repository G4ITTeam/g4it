/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apibatchevaluation.listener;

import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.apiinventory.modeldb.InventoryEvaluationReport;
import com.soprasteria.g4it.backend.apiinventory.repository.InventoryRepository;
import com.soprasteria.g4it.backend.common.utils.EvaluationBatchStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

import java.util.Objects;

import static com.soprasteria.g4it.backend.common.utils.Constants.STARTED_PROGRESS_PERCENTAGE;
import static com.soprasteria.g4it.backend.common.utils.EvaluationBatchStatus.CALCUL_IN_PROGRESS;

/**
 * Inventory Evaluation Job Execution Listener.
 */
@RequiredArgsConstructor
public class InventoryEvaluationJobListener implements JobExecutionListener {

    /**
     * Inventory repository to update inventory after job.
     */
    private final InventoryRepository inventoryRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public void beforeJob(final JobExecution jobExecution) {
        final Inventory processedInventory = inventoryRepository.findById(Objects.requireNonNull(jobExecution.getJobParameters().getLong("inventory.id"))).orElseThrow();
        processedInventory.addEvaluationReport(InventoryEvaluationReport.builder()
                .batchName(jobExecution.getJobParameters().getString("batch.name"))
                .createTime(jobExecution.getCreateTime())
                .batchStatusCode(EvaluationBatchStatus.DATA_EXTRACTION.name())
                .progressPercentage(STARTED_PROGRESS_PERCENTAGE)
                .isApplicationAggregated(false)
                .build());
        inventoryRepository.save(processedInventory);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterJob(final JobExecution jobExecution) {
        final Inventory processedInventory = inventoryRepository.findById(Objects.requireNonNull(jobExecution.getJobParameters().getLong("inventory.id"))).orElseThrow();
        final InventoryEvaluationReport currentReport = processedInventory.getEvaluationReports().stream().filter(report -> report.getBatchName().equals(jobExecution.getJobParameters().getString("batch.name"))).findFirst().orElseThrow();
        currentReport.setEndTime(jobExecution.getEndTime());
        /* If Calculation is submitted successfully , Change batch status to 'CALCUL_IN_PROGRESS' */
        if (BatchStatus.COMPLETED.name().equals(jobExecution.getStatus().name()))
            currentReport.setBatchStatusCode(CALCUL_IN_PROGRESS.name());
        else currentReport.setBatchStatusCode(jobExecution.getStatus().name());
        inventoryRepository.save(processedInventory);
    }
}
