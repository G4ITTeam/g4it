/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apibatchexport.config.step;

import com.soprasteria.g4it.backend.apibatchexport.business.InventoryExportService;
import com.soprasteria.g4it.backend.apibatchexport.tasklet.CleanExportWorkingFolderTasklet;
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
public class CleanExportWorkingFolderStepConfiguration {

    /**
     * Step Configuration to remove working folder
     *
     * @param jobRepository                   Spring Batch Job Repository.
     * @param transactionManager              the transaction manager (since Spring Batch v5).
     * @param cleanExportWorkingFolderTasklet Tasklet to remove working folder
     * @return configured step.
     */
    @Bean
    public Step cleanExportWorkingFolderStep(final JobRepository jobRepository,
                                             final PlatformTransactionManager transactionManager,
                                             final CleanExportWorkingFolderTasklet cleanExportWorkingFolderTasklet
    ) {
        return new StepBuilder("cleanWorkingEvaluationFolderStep", jobRepository)
                .tasklet(cleanExportWorkingFolderTasklet, transactionManager)
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
    public CleanExportWorkingFolderTasklet cleanExportWorkingFolderTasklet(
            @Value("#{jobParameters['local.working.folder']}") final String localWorkingFolder,
            @Value("#{jobParameters['batch.name']}") final String batchName,
            final InventoryExportService inventoryExportService) {
        return new CleanExportWorkingFolderTasklet(localWorkingFolder, batchName, inventoryExportService);
    }

}
