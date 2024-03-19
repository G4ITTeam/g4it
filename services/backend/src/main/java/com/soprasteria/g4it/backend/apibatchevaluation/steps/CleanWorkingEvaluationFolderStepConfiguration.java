/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apibatchevaluation.steps;

import com.soprasteria.g4it.backend.apibatchevaluation.tasklet.CleanWorkingFolderTasklet;
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
public class CleanWorkingEvaluationFolderStepConfiguration {

    /**
     * Step Configuration to remove working folder
     *
     * @param jobRepository                       Spring Batch Job Repository.
     * @param transactionManager                  the transaction manager (since Spring Batch v5).
     * @param cleanWorkingEvaluationFolderTasklet Tasklet to remove working folder
     * @return configured step.
     */
    @Bean
    public Step cleanWorkingEvaluationFolderStep(final JobRepository jobRepository,
                                                 final PlatformTransactionManager transactionManager,
                                                 final CleanWorkingFolderTasklet cleanWorkingEvaluationFolderTasklet) {
        return new StepBuilder("cleanWorkingFolderStep", jobRepository)
                .tasklet(cleanWorkingEvaluationFolderTasklet, transactionManager)
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
    public CleanWorkingFolderTasklet cleanWorkingEvaluationFolderTasklet(
            @Value("#{jobParameters['local.working.folder']}") final String localWorkingFolder) {
        return new CleanWorkingFolderTasklet(localWorkingFolder);
    }

}
