/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apibatchloading.steps.clean.tasklet;

import com.soprasteria.g4it.backend.common.filesystem.business.FileStorage;
import com.soprasteria.g4it.backend.common.filesystem.model.FileFolder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.util.FileSystemUtils;

import java.nio.file.Path;

/**
 * Remove working folder Tasklet
 */
@Slf4j
@AllArgsConstructor
public class CleanWorkingFolderTasklet implements Tasklet {

    /**
     * Path to working Folder
     */
    private final String workingFolder;

    /**
     * Whether working folder should be deleted
     */
    private final boolean shouldDeleteWorkingFolder;

    /**
     * Session Path of working Folder
     */
    private final String sessionPath;

    /**
     * FileStorage system
     */
    private final FileStorage fileStorage;

    /**
     * {@inheritDoc}
     */
    @Override
    public RepeatStatus execute(final StepContribution stepContribution, final ChunkContext chunkContext) throws Exception {
        if (shouldDeleteWorkingFolder) {
            log.info("Deleting local folder {}", workingFolder);
            FileSystemUtils.deleteRecursively(Path.of(workingFolder));
            log.info("Deleting remove folder {}/{}", FileFolder.WORK, sessionPath);
            fileStorage.deleteFolder(FileFolder.WORK, sessionPath);
        }
        return RepeatStatus.FINISHED;
    }

}
