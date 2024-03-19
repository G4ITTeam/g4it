/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apibatchloading.steps.common.tasklet;

import com.soprasteria.g4it.backend.common.filesystem.model.FileFolder;
import com.soprasteria.g4it.backend.common.filesystem.model.FileStorage;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

/**
 * Tasklet de récupération du fichier à intégrer.
 */
public class MoveFileTasklet implements Tasklet {

    /**
     * Le système de gestion de fichier
     */
    private final FileStorage fileStorage;

    /**
     * Le fichier en entrée à traiter.
     */
    private final String inputFileName;

    /**
     * Constructeur paramétré de la tasklet.
     *
     * @param inputFileName le fichier en entrée à trairer.
     */
    public MoveFileTasklet(final FileStorage fileStorage, final String inputFileName) {
        this.fileStorage = fileStorage;
        this.inputFileName = inputFileName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RepeatStatus execute(final StepContribution stepContribution, final ChunkContext chunkContext) throws Exception {
        this.fileStorage.move(FileFolder.INPUT, FileFolder.WORK, inputFileName);

        return RepeatStatus.FINISHED;
    }

}
