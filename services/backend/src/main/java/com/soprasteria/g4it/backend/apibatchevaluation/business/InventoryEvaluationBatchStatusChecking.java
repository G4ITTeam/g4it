/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apibatchevaluation.business;

import com.soprasteria.g4it.backend.apibatchevaluation.repository.InventoryEvaluationReportRepository;
import com.soprasteria.g4it.backend.apiinventory.modeldb.InventoryEvaluationReport;
import com.soprasteria.g4it.backend.common.utils.EvaluationBatchStatus;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static com.soprasteria.g4it.backend.config.EvaluationBatchConfiguration.EVALUATE_INVENTORY_JOB;


/**
 * Check the batch status when starting greenIt.
 */
@Slf4j
@Component
@Transactional
@Profile("!test")
public class InventoryEvaluationBatchStatusChecking {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private InventoryEvaluationReportRepository inventoryEvaluationReportRepository;

    /**
     * Check evaluation batch status on start up.
     * Update jobExecutions and report when status is STARTED or STARTING with exitStatus UNKNOWN.
     */
    @PostConstruct
    public void init() {
        log.info("Start evalution batch context checking");
        final List<JobExecution> jobExecutions = jobRepository.findJobInstancesByName(EVALUATE_INVENTORY_JOB, 0, Integer.MAX_VALUE)
                .stream().map(jobRepository::findJobExecutions).flatMap(Collection::stream)
                .filter(job -> List.of(BatchStatus.STARTED, BatchStatus.STARTING).contains(job.getStatus()) && ExitStatus.UNKNOWN.equals(job.getExitStatus()))
                .toList();
        jobExecutions.forEach(this::updateContextOnStart);
        log.info("End evaluation batch context checking");
    }

    /**
     * Update context.
     *
     * @param jobExecution the spring batch jobExecution in unknown state.
     */
    private void updateContextOnStart(final JobExecution jobExecution) {
        log.warn("Update evaluation job execution {}.", jobExecution.getJobId());
        jobExecution.setLastUpdated(LocalDateTime.now());
        jobExecution.setStatus(BatchStatus.FAILED);
        jobExecution.setExitStatus(ExitStatus.FAILED);
        jobRepository.update(jobExecution);

        final Long inventoryId = jobExecution.getJobParameters().getLong(InventoryEvaluationJobService.INVENTORY_ID_JOB_PARAM);
        inventoryEvaluationReportRepository.findByInventoryId(inventoryId).stream()
                .filter(report -> List.of(BatchStatus.STARTED.name(), EvaluationBatchStatus.CALCUL_IN_PROGRESS.name()).contains(report.getBatchStatusCode()))
                .forEach(report -> updateEvaluationReport(report, jobExecution));
    }

    /**
     * Update evaluation report.
     *
     * @param evaluationReport the evaluation report with STARTED state.
     * @param jobExecution     job execution.
     */
    private void updateEvaluationReport(final InventoryEvaluationReport evaluationReport, final JobExecution jobExecution) {
        final String batchName = jobExecution.getJobParameters().getString(InventoryEvaluationJobService.BATCH_NAME_JOB_PARAM);
        final String inventoryName = jobExecution.getJobParameters().getString(InventoryEvaluationJobService.INVENTORY_NAME_JOB_PARAM);
        final String organization = jobExecution.getJobParameters().getString(InventoryEvaluationJobService.ORGANIZATION);
        log.warn("Update evaluation report {} linked to inventory {} of organization {}.", batchName, inventoryName, organization);
        evaluationReport.setBatchStatusCode(BatchStatus.FAILED.name());
        evaluationReport.setEndTime(LocalDateTime.now());
        inventoryEvaluationReportRepository.save(evaluationReport);
    }
}
