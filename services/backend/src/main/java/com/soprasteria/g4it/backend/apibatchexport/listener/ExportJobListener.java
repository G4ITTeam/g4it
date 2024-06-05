/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apibatchexport.listener;

import com.soprasteria.g4it.backend.apibatchexport.modeldb.ExportReport;
import com.soprasteria.g4it.backend.apibatchexport.repository.ExportReportRepository;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.apiinventory.repository.InventoryRepository;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.common.utils.ExportBatchStatus;
import com.soprasteria.g4it.backend.common.utils.LocalFileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

import java.util.Optional;


/**
 * Export Job Execution Listener.
 */
@RequiredArgsConstructor
@Slf4j
public class ExportJobListener implements JobExecutionListener {

    /**
     * Inventory repository to update inventory after job.
     */
    private final ExportReportRepository exportReportRepository;

    /**
     * Repository to access inventory data.
     */
    private final InventoryRepository inventoryRepository;

    /**
     * The processed export.
     */
    private ExportReport processedExportReport;

    /**
     * {@inheritDoc}
     */
    @Override
    public void beforeJob(final JobExecution jobExecution) {
        final Long inventoryId = jobExecution.getJobParameters().getLong("inventory.id");
        final Inventory currentInventory = inventoryRepository.findById(Optional.ofNullable(inventoryId).orElseThrow()).orElseThrow();
        processedExportReport = Optional.ofNullable(currentInventory.getExportReport()).orElse(ExportReport.builder()
                .inventoryId(inventoryId)
                .build());
        processedExportReport.setBatchName(jobExecution.getJobParameters().getString("batch.name"));
        processedExportReport.setBatchCreateTime(jobExecution.getCreateTime());
        processedExportReport.setStatusCode(ExportReport.REQUESTED);
        exportReportRepository.save(processedExportReport);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterJob(final JobExecution jobExecution) {
        processedExportReport.setStatusCode(jobExecution.getStatus().name());
        processedExportReport.setBatchEndTime(jobExecution.getEndTime());
        if (jobExecution.getStatus().name().equals(BatchStatus.COMPLETED.name())) {
            processedExportReport.setStatusCode(ExportBatchStatus.EXPORT_GENERATED.name());
            processedExportReport.setExportFilename(jobExecution.getExecutionContext().getString(Constants.FILE_URL_CONTEXT_KEY));
            processedExportReport.setExportFileSize(jobExecution.getExecutionContext().getLong(Constants.FILE_LENGTH_CONTEXT_KEY));
        } else {
            processedExportReport.setStatusCode(jobExecution.getStatus().name());
        }
        exportReportRepository.save(processedExportReport);
        LocalFileUtils.cleanFolder(jobExecution.getJobParameters().getString("local.working.folder"));
    }
}
