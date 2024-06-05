/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apibatchexport.config.step;

import com.soprasteria.g4it.backend.apibatchexport.business.InventoryExportService;
import com.soprasteria.g4it.backend.apibatchexport.tasklet.UploadExportResultTasklet;
import com.soprasteria.g4it.backend.common.filesystem.model.FileStorage;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.nio.file.Path;

@Configuration
public class UploadExportResultStepConfiguration {

    /**
     * Step Configuration to upload files at the end
     *
     * @param jobRepository             Spring Batch Job Repository.
     * @param transactionManager        the transaction manager (since Spring Batch v5).
     * @param uploadExportResultTasklet Tasklet for export results
     * @return configured step.
     */
    @Bean
    public Step uploadExportResultStep(final JobRepository jobRepository,
                                       final PlatformTransactionManager transactionManager,
                                       final UploadExportResultTasklet uploadExportResultTasklet) {
        return new StepBuilder("uploadExportResultStep", jobRepository)
                .tasklet(uploadExportResultTasklet, transactionManager)
                .build();
    }

    /**
     * Tasklet Config to upload OK file results
     *
     * @param localWorkingFolder generated local working folder
     * @param exportFileStorage  file storage to handle files operations (can be use because it's mount by the job service).
     * @return configured tasklet.
     */
    @Bean
    @StepScope
    public UploadExportResultTasklet uploadExportResultTasklet(
            @Value("#{jobParameters['local.working.folder']}") final Path localWorkingFolder,
            @Value("#{jobParameters['inventory.id']}") final long inventoryId,
            final FileStorage exportFileStorage,
            @Value("#{jobParameters['batch.name']}") final String batchName,
            final InventoryExportService inventoryExportService) {
        return new UploadExportResultTasklet(exportFileStorage, localWorkingFolder, inventoryId, batchName, inventoryExportService);
    }

}
