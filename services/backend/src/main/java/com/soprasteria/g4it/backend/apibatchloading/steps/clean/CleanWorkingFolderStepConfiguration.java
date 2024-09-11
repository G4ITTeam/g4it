/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apibatchloading.steps.clean;

import com.soprasteria.g4it.backend.apibatchloading.steps.clean.tasklet.CleanWorkingFolderTasklet;
import com.soprasteria.g4it.backend.common.filesystem.business.FileStorage;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Step configuration to upload result to filesystem
 */
@Configuration
public class CleanWorkingFolderStepConfiguration {

    /**
     * Step Configuration to remove working folder
     *
     * @param jobRepository             Spring Batch Job Repository.
     * @param transactionManager        the transaction manager (since Spring Batch v5).
     * @param cleanWorkingFolderTasklet Tasklet to remove working folder
     * @return configured step.
     */
    @Bean
    public Step cleanWorkingFolderStep(final JobRepository jobRepository,
                                       final PlatformTransactionManager transactionManager,
                                       final CleanWorkingFolderTasklet cleanWorkingFolderTasklet) {
        return new StepBuilder("cleanWorkingFolderStep", jobRepository)
                .tasklet(cleanWorkingFolderTasklet, transactionManager)
                .build();
    }

    /**
     * Tasklet Config to remove Working Folder
     *
     * @param localWorkingFolder generated local working folder
     * @return configured tasklet.
     */
    @Bean
    @StepScope
    public CleanWorkingFolderTasklet cleanWorkingFolderTasklet(
            @Value("#{jobParameters['local.working.folder']}") final String localWorkingFolder,
            @Value("#{jobParameters['delete.local.working.folder']}") final String shouldDeleteWorkingFolder,
            @Value("#{jobExecutionContext['session.path']}") final String sessionPath,
            final FileStorage loadingFileStorage
    ) {
        return new CleanWorkingFolderTasklet(localWorkingFolder, Boolean.parseBoolean(shouldDeleteWorkingFolder), sessionPath, loadingFileStorage);
    }

}
