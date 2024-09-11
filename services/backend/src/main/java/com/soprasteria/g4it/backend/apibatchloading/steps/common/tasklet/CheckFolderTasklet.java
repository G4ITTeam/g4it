/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apibatchloading.steps.common.tasklet;

import com.soprasteria.g4it.backend.common.filesystem.business.FileStorage;
import com.soprasteria.g4it.backend.common.filesystem.model.FileFolder;
import com.soprasteria.g4it.backend.common.filesystem.model.FileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import java.io.IOException;

/**
 * Tasklet de récupération du fichier à intégrer.
 */
public class CheckFolderTasklet implements Tasklet {

    public static final String PROCESS_EXIT_STATUS = "PROCESS";
    public static final String IGNORE_EXIT_STATUS = "IGNORE";

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CheckFolderTasklet.class);

    /**
     * Le système de gestion de fichier
     */
    private final FileStorage storage;
    private final String sessionPath;
    private final FileType type;

    /**
     * Parametrized constructor
     *
     * @param fileStorage file storage
     * @param sessionPath session path
     */
    public CheckFolderTasklet(final FileStorage fileStorage, final String sessionPath, final FileType type) {
        this.storage = fileStorage;
        this.sessionPath = sessionPath;
        this.type = type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RepeatStatus execute(final StepContribution stepContribution, final ChunkContext chunkContext) throws Exception {
        try {
            if (!storage.hasFileInSubfolder(FileFolder.WORK, sessionPath, type)) {
                LOGGER.warn("No {} files found, ignoring", type);
                stepContribution.getStepExecution().setExitStatus(new ExitStatus(IGNORE_EXIT_STATUS));
                return RepeatStatus.FINISHED;
            }
            LOGGER.info("Found {} files, start processing", type);
            stepContribution.getStepExecution().setExitStatus(new ExitStatus(PROCESS_EXIT_STATUS));
            return RepeatStatus.FINISHED;
        } catch (IOException e) {
            LOGGER.warn("Assuming there is no {} to process as exception occurred while trying to list them: {}", type, e.getMessage());
            stepContribution.getStepExecution().setExitStatus(new ExitStatus(IGNORE_EXIT_STATUS));
            return RepeatStatus.FINISHED;
        }
    }

}
