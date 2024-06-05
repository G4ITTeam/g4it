/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apibatchexport.tasklet;

import com.soprasteria.g4it.backend.apibatchexport.business.InventoryExportService;
import com.soprasteria.g4it.backend.common.utils.ExportBatchStatus;
import com.soprasteria.g4it.backend.common.utils.LocalFileUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

/**
 * Remove working folder Tasklet
 */
@Slf4j
@AllArgsConstructor
public class CleanExportWorkingFolderTasklet implements Tasklet {

    /**
     * Path to working Folder
     */
    private final String workingFolder;

    /**
     * Batch Name
     */
    private final String batchName;

    /**
     * Export Service to update batch status
     */
    private InventoryExportService inventoryExportService;

    /**
     * {@inheritDoc}
     */
    @Override
    public RepeatStatus execute(final StepContribution stepContribution, final ChunkContext chunkContext) throws Exception {
        inventoryExportService.updateBatchStatusCode(batchName, ExportBatchStatus.CLEANING_WORKING_FOLDERS.name());
        LocalFileUtils.cleanFolder(workingFolder);
        return RepeatStatus.FINISHED;
    }

}
