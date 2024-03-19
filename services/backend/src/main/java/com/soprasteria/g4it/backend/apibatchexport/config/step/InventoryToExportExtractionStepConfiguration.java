/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apibatchexport.config.step;

import com.soprasteria.g4it.backend.apibatchexport.tasklet.InventoryToExportTasklet;
import com.soprasteria.g4it.backend.apiinventory.repository.InventoryRepository;
import com.soprasteria.g4it.backend.common.filesystem.model.FileMapperInfo;
import com.soprasteria.g4it.backend.common.filesystem.model.FileType;
import com.soprasteria.g4it.backend.common.filesystem.model.Header;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * The Inventory extraction Step Configuration.
 */
@Configuration
public class InventoryToExportExtractionStepConfiguration {

    @Bean
    public Flow inventoryToExportExtractionFlow(final Step inventoryToExportExtractionStep) {
        return new FlowBuilder<SimpleFlow>("inventoryToExportExtractionFlow")
                .start(inventoryToExportExtractionStep)
                .build();
    }

    /**
     * Application extraction Step configuration.
     *
     * @param jobRepository            Spring Batch Job Repository.
     * @param transactionManager       the transaction manager (since Spring Batch v5).
     * @param inventoryToExportTasklet the tasklet to extract Inventory data from database.
     * @return the configured Step.
     */
    @Bean
    public Step inventoryToExportExtractionStep(final JobRepository jobRepository,
                                                final PlatformTransactionManager transactionManager,
                                                final Tasklet inventoryToExportTasklet) {
        return new StepBuilder("inventoryToExportExtractionStep", jobRepository)
                .tasklet(inventoryToExportTasklet, transactionManager)
                .build();
    }

    /**
     * Tasklet Config to extract Inventory data.
     *
     * @param localWorkingFolder  generated local working folder
     * @param inventoryId         the inventory identifier.
     * @param inventoryRepository repository to access inventory data.
     * @param fileInfo            to get inventory header.
     * @return configured tasklet.
     */
    @Bean
    @StepScope
    public InventoryToExportTasklet inventoryToExportTasklet(
            @Value("#{jobParameters['local.working.folder']}") final String localWorkingFolder,
            @Value("#{jobParameters['inventory.id']}") final long inventoryId,
            final InventoryRepository inventoryRepository,
            final FileMapperInfo fileInfo) {
        return new InventoryToExportTasklet(localWorkingFolder,
                fileInfo.getMapping(FileType.INVENTORY).stream().map(Header::getName).toList(),
                inventoryId,
                inventoryRepository);
    }
}
