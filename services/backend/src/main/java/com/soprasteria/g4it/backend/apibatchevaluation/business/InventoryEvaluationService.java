/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apibatchevaluation.business;

import com.soprasteria.g4it.backend.apibatchevaluation.repository.InventoryEvaluationReportRepository;
import com.soprasteria.g4it.backend.apibatchexport.business.InventoryExportService;
import com.soprasteria.g4it.backend.apifiles.business.FileSystemService;
import com.soprasteria.g4it.backend.apiindicator.business.IndicatorService;
import com.soprasteria.g4it.backend.apiinventory.business.InventoryService;
import com.soprasteria.g4it.backend.apiinventory.model.InventoryBO;
import com.soprasteria.g4it.backend.apiinventory.model.InventoryExportReportBO;
import com.soprasteria.g4it.backend.apiinventory.modeldb.InventoryEvaluationReport;
import com.soprasteria.g4it.backend.apiuser.business.OrganizationService;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import com.soprasteria.g4it.backend.common.filesystem.model.FileFolder;
import com.soprasteria.g4it.backend.common.utils.EvaluationBatchStatus;
import com.soprasteria.g4it.backend.common.utils.ExportBatchStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobExecutionNotRunningException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Inventory Evaluation service.
 */
@Service
@Slf4j
public class InventoryEvaluationService {

    @Autowired
    InventoryEvaluationReportRepository inventoryEvaluationReportRepository;
    /**
     * Inventory Export Job Service.
     */
    @Autowired
    InventoryExportService inventoryExportService;
    /**
     * File System service.
     */
    @Autowired
    FileSystemService fileSystemService;
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
    /**
     * Job Operator.
     */
    @Autowired
    private JobOperator jobOperator;
    /**
     * Job Explorer.
     */
    @Autowired
    private JobExplorer jobExplorer;

    @Autowired
    private OrganizationService organizationService;

    /**
     * Launch loading batch job.
     *
     * @param subscriber     the subscriber.
     * @param inventoryId    the inventory id.
     * @param organizationId the organization's id.
     * @return spring batch job instance id.
     */
    public Long launchEvaluationBatchJob(final String subscriber, final Long inventoryId, final Long organizationId) {
        final Organization linkedOrganization = organizationService.getOrganizationById(organizationId);
        final String organization = linkedOrganization.getName();
        final InventoryBO inventory = inventoryService.getInventory(subscriber, organizationId, inventoryId);
        // Remove last indicators if present.
        inventoryService.getLastBatchName(inventory).ifPresent(batchName -> indicatorService.deleteIndicators(batchName));

        // Handle Previous Export Files
        InventoryExportReportBO exportReport = inventory.getExportReport();
        if (exportReport == null) {
            // Launch evaluation.
            return inventoryEvaluationJobService.launchInventoryEvaluation(organization, inventory.getName(), inventoryId, organizationId);
        }

        // force stopping export job if one is running
        jobExplorer.findRunningJobExecutions("exportJob").stream()
                .filter(job -> inventoryId.equals(job.getJobParameters().getLong("inventory.id")))
                .findFirst()
                .ifPresent(job -> {
                    try {
                        log.info("Stopping export job for inventory id {}", inventoryId);
                        jobOperator.stop(job.getJobId());
                    } catch (NoSuchJobExecutionException e) {
                        log.info("The export job related to inventory id {} is already stopped", inventoryId);
                    } catch (JobExecutionNotRunningException e) {
                        log.info("The export job related to inventory id {} is not running", inventoryId);
                    }
                });

        // Delete last exported file if it exists
        if (exportReport.getResultFileUrl() != null) {
            String deletedFilePath = fileSystemService.deleteFile(subscriber, organizationId, FileFolder.EXPORT, exportReport.getResultFileUrl());
            if (deletedFilePath == null) {
                log.info("No file to delete for export job of inventory {}", inventoryId);
            } else {
                log.info("File Deleted : {} for inventory {}", deletedFilePath, inventoryId);
            }
        }
        // Update export batch status to 'REMOVED'
        log.info("Setting Export Batch status '{}' to {}", exportReport.getBatchName(), ExportBatchStatus.REMOVED.name());
        inventoryExportService.updateBatchStatusCode(exportReport.getBatchName(), ExportBatchStatus.REMOVED.name());

        // Launch evaluation.
        return inventoryEvaluationJobService.launchInventoryEvaluation(organization, inventory.getName(), inventoryId, organizationId);
    }


    /**
     * Delete evaluation batch job.
     *
     * @param organizationId the organization's id.
     * @param inventoryId    the inventory id.
     */
    public void deleteEvaluationBatchJob(final Long organizationId, final Long inventoryId) {
        inventoryEvaluationJobService.deleteJobInstances(organizationId, inventoryId);
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
