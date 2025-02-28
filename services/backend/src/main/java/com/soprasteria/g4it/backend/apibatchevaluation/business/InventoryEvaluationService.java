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
import com.soprasteria.g4it.backend.apiindicator.utils.Constants;
import com.soprasteria.g4it.backend.apiinventory.business.InventoryService;
import com.soprasteria.g4it.backend.apiinventory.model.InventoryBO;
import com.soprasteria.g4it.backend.apiinventory.model.InventoryExportReportBO;
import com.soprasteria.g4it.backend.apiinventory.modeldb.InventoryEvaluationReport;
import com.soprasteria.g4it.backend.apiuser.business.OrganizationService;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import com.soprasteria.g4it.backend.apiuser.repository.SubscriberRepository;
import com.soprasteria.g4it.backend.common.criteria.CriteriaService;
import com.soprasteria.g4it.backend.common.filesystem.model.FileFolder;
import com.soprasteria.g4it.backend.common.utils.EvaluationBatchStatus;
import com.soprasteria.g4it.backend.common.utils.ExportBatchStatus;
import com.soprasteria.g4it.backend.external.numecoeval.business.NumEcoEvalRemotingService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobExecutionNotRunningException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;

import static com.soprasteria.g4it.backend.common.utils.Constants.COMPLETE_PROGRESS_PERCENTAGE;

/**
 * Inventory Evaluation service.
 */
@Service
@Slf4j
public class InventoryEvaluationService {


    @Autowired
    InventoryEvaluationReportRepository inventoryEvaluationReportRepository;

    @Autowired
    SubscriberRepository subscriberRepository;
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

    @Autowired
    private NumEcoEvalRemotingService numEcoEvalRemotingService;

    @Autowired
    private AggregationService aggregationService;

    @Autowired
    private CriteriaService criteriaService;

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

        List<String> criteriaKeyList = criteriaService.getSelectedCriteriaForInventory(subscriber, organizationId, inventory.getCriteria()).active();

        // Remove last indicators and aggregated indicators if present.
        inventoryService.getLastBatchName(inventory).ifPresent(batchName -> indicatorService.deleteIndicators(batchName));

        // Handle Previous Export Files
        InventoryExportReportBO exportReport = inventory.getExportReport();
        if (exportReport == null) {
            // Launch evaluation.
            return inventoryEvaluationJobService.launchInventoryEvaluation(subscriber, organization, inventory.getName(), inventoryId, organizationId, criteriaKeyList);
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
        return inventoryEvaluationJobService.launchInventoryEvaluation(subscriber, organization, inventory.getName(), inventoryId, organizationId, criteriaKeyList);
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

    /**
     * @param batchName    batch name
     * @param criteriaList list of criteria to evaluate impacts on
     */

    public void setCriteriaList(String batchName, List<String> criteriaList) {
        InventoryEvaluationReport evaluationReport = inventoryEvaluationReportRepository.findByBatchName(batchName);

        List<String> criteriaToSet = Optional.ofNullable(criteriaList).orElse(Constants.CRITERIA_LIST);
        evaluationReport.setCriteria(criteriaToSet);

        inventoryEvaluationReportRepository.save(evaluationReport);
    }

    /**
     * Calculate the progress percentage of NumEcoEval calculation process
     * If NumEcoEval calculation process is 100%, then modify the batch status to AGGREGATION_IN_PROGRESS
     * And update the evaluation report
     */
    @Transactional
    public void calculateProgressPercentage() {

        inventoryEvaluationReportRepository.findByBatchStatusCode(EvaluationBatchStatus.CALCUL_IN_PROGRESS.name(), Limit.of(100))
                .forEach(report -> {
                    String calculProgressPercentage = numEcoEvalRemotingService.getCalculationsProgress(report.getBatchName(), String.valueOf(report.getInventory().getOrganization().getId()));
                    if (calculProgressPercentage == null) return;

                    int calculationValue = Integer.parseInt(calculProgressPercentage.split("%")[0]);
                    report.setProgressPercentage(new DecimalFormat("#").format(calculationValue * 0.8) + "%");
                    log.info("Updating calculation progress percentage to '{}' for batch : {} ", calculProgressPercentage, report.getBatchName());

                    if (COMPLETE_PROGRESS_PERCENTAGE.equals(calculProgressPercentage)) {
                        report.setBatchStatusCode(EvaluationBatchStatus.AGGREGATION_IN_PROGRESS.name());
                        log.info("Updating batch status to 'AGGREGATION_IN_PROGRESS' of batch : '{}' ", report.getBatchName());
                    }
                    inventoryEvaluationReportRepository.save(report);
                });

    }

    /**
     * Aggregate indicators
     * For each evaluation report in status AGGREGATION_IN_PROGRESS
     * - execute aggregation of indicators
     * - update the status to COMPLETED
     */
    public void aggregateIndicatorsData() {

        inventoryEvaluationReportRepository.findByBatchStatusCode(EvaluationBatchStatus.AGGREGATION_IN_PROGRESS.name(), Limit.of(10))
                .forEach(report -> {
                    var start = System.currentTimeMillis();
                    aggregationService.aggregateBatchData(report.getBatchName());

                    report.setProgressPercentage(COMPLETE_PROGRESS_PERCENTAGE);
                    report.setBatchStatusCode(BatchStatus.COMPLETED.name());
                    report.setIsApplicationAggregated(true);

                    log.info("Aggregation time: {}s, updating batch status to 'COMPLETED' of batch : '{}'",
                            (System.currentTimeMillis() - start) / 1000,
                            report.getBatchName()
                    );
                    inventoryEvaluationReportRepository.save(report);
                });

    }
}
