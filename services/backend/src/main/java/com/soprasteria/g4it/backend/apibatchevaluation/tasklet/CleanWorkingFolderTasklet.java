/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apibatchevaluation.tasklet;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.util.FileSystemUtils;

import java.nio.file.Path;

/**
 * Remove working folder Tasklet
 */
@AllArgsConstructor
public class CleanWorkingFolderTasklet implements Tasklet {

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CleanWorkingFolderTasklet.class);

    /**
     * Path to working Folder
     */
    private final String workingFolder;

    /**
     * {@inheritDoc}
     */
    @Override
    public RepeatStatus execute(final StepContribution stepContribution, final ChunkContext chunkContext) throws Exception {
        LOGGER.info("Deleting local folder {}", workingFolder);
        FileSystemUtils.deleteRecursively(Path.of(workingFolder));
        return RepeatStatus.FINISHED;
    }

}
