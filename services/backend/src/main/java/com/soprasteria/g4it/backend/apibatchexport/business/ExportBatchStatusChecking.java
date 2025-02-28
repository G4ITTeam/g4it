/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apibatchexport.business;

import com.soprasteria.g4it.backend.apibatchexport.config.ExportBatchConfiguration;
import com.soprasteria.g4it.backend.apibatchexport.modeldb.ExportReport;
import com.soprasteria.g4it.backend.apibatchexport.repository.ExportReportRepository;
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

import static com.soprasteria.g4it.backend.apibatchexport.business.ExportJobService.*;
import static com.soprasteria.g4it.backend.common.utils.Constants.STATUS_IN_PROGRESS;


/**
 * Check the batch status when starting greenIt.
 */
@Slf4j
@Component
@Transactional
@Profile("!test")
public class ExportBatchStatusChecking {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private ExportReportRepository exportReportRepository;

    /**
     * Check evaluation batch status on start up.
     * Update jobExecutions and report when status is STARTED or STARTING with exitStatus UNKNOWN.
     */
    @PostConstruct
    public void init() {
        log.info("Start export batch context checking");
        final List<JobExecution> jobExecutions = jobRepository.findJobInstancesByName(ExportBatchConfiguration.EXPORT_JOB, 0, Integer.MAX_VALUE)
                .stream().map(jobRepository::findJobExecutions).flatMap(Collection::stream)
                .filter(job -> List.of(BatchStatus.STARTED, BatchStatus.STARTING).contains(job.getStatus()) && ExitStatus.UNKNOWN.equals(job.getExitStatus()))
                .toList();
        jobExecutions.forEach(this::updateContextOnStart);
        log.info("End export batch context checking");
    }

    /**
     * Update context.
     *
     * @param jobExecution the spring batch jobExecution in unknown state.
     */
    private void updateContextOnStart(final JobExecution jobExecution) {
        log.warn("Update export job execution {}.", jobExecution.getJobId());
        jobExecution.setLastUpdated(LocalDateTime.now());
        jobExecution.setStatus(BatchStatus.FAILED);
        jobExecution.setExitStatus(ExitStatus.FAILED);
        jobRepository.update(jobExecution);

        final Long inventoryId = jobExecution.getJobParameters().getLong(INVENTORY_ID_JOB_PARAM);
        exportReportRepository.findByInventoryId(inventoryId).stream()
                .filter(report -> STATUS_IN_PROGRESS.contains(report.getStatusCode()))
                .forEach(report -> updateExportReport(report, jobExecution));
    }

    /**
     * Update evaluation report.
     *
     * @param exportReport the evaluation report with STARTED state.
     * @param jobExecution the job execution.
     */
    private void updateExportReport(final ExportReport exportReport, final JobExecution jobExecution) {
        final String batchName = jobExecution.getJobParameters().getString(BATCH_NAME_JOB_PARAM);
        final String inventoryName = jobExecution.getJobParameters().getString(INVENTORY_NAME_JOB_PARAM);
        final String organization = jobExecution.getJobParameters().getString(ORGANIZATION_JOB_PARAM);
        log.warn("Update export report {} linked to inventory {} of organization {}.", batchName, inventoryName, organization);
        exportReport.setStatusCode(BatchStatus.FAILED.name());
        exportReport.setBatchEndTime(LocalDateTime.now());
        exportReportRepository.save(exportReport);
    }
}
