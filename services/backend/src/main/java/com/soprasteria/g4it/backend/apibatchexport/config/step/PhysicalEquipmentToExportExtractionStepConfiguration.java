/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apibatchexport.config.step;

import com.soprasteria.g4it.backend.apiinventory.modeldb.PhysicalEquipment;
import com.soprasteria.g4it.backend.apiinventory.repository.PhysicalEquipmentRepository;
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
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.nio.file.Path;
import java.util.Map;

/**
 * The PhysicalEquipment extraction Step Configuration.
 */
@Configuration
public class PhysicalEquipmentToExportExtractionStepConfiguration {

    @Bean
    public Flow physicalEquipmentToExportExtractionFlow(final Step physicalEquipmentToExportExtractionStep) {
        return new FlowBuilder<SimpleFlow>("physicalEquipmentToExportExtractionFlow")
                .start(physicalEquipmentToExportExtractionStep)
                .build();
    }

    /**
     * PhysicalEquipment extraction Step configuration.
     *
     * @param jobRepository                              Spring Batch Job Repository.
     * @param transactionManager                         the transaction manager (since Spring Batch v5).
     * @param extractPhysicalEquipmentToExportDataReader RepositoryItemReader to extract PhysicalEquipment data from database.
     * @param extractedPhysicalEquipmentToExportWriter   FlatFileItemReader to write extraction file.
     * @param chunkValue                                 chunk size.
     * @return the configured Step.
     */
    @Bean
    public Step physicalEquipmentToExportExtractionStep(final JobRepository jobRepository,
                                                        final PlatformTransactionManager transactionManager,
                                                        final RepositoryItemReader<PhysicalEquipment> extractPhysicalEquipmentToExportDataReader,
                                                        final FlatFileItemWriter<PhysicalEquipment> extractedPhysicalEquipmentToExportWriter,
                                                        @Value("${export.batch.chunk}") final Integer chunkValue) {
        return new StepBuilder("physicalEquipmentToExportExtractionStep", jobRepository)
                .<PhysicalEquipment, PhysicalEquipment>chunk(chunkValue, transactionManager)
                .reader(extractPhysicalEquipmentToExportDataReader)
                .writer(extractedPhysicalEquipmentToExportWriter)
                .taskExecutor(physicalEquipmentToExportTaskExecutor())
                .build();
    }

    /**
     * Task executor.
     *
     * @return the simpleAsyncTaskExecutor
     */
    @Bean
    public TaskExecutor physicalEquipmentToExportTaskExecutor() {
        return new SimpleAsyncTaskExecutor("physicalEquipmentToExportTaskExecutor");
    }

    /**
     * RepositoryItemReader to extract PhysicalEquipment data from database.
     *
     * @param physicalEquipmentRepository the repository to access PhysicalEquipment data from DataBase.
     * @param inventoryId                 the inventory Id.
     * @param pageSize                    the repository reader page size.
     * @return the configured reader.
     */
    @Bean
    @StepScope
    public RepositoryItemReader<PhysicalEquipment> extractPhysicalEquipmentToExportDataReader(final PhysicalEquipmentRepository physicalEquipmentRepository,
                                                                                              @Value("#{jobParameters['inventory.id']}") final long inventoryId,
                                                                                              @Value("${export.batch.page-size}") final Integer pageSize) {
        final RepositoryItemReaderBuilder<PhysicalEquipment> builder = new RepositoryItemReaderBuilder<>();
        return builder.name("extractPhysicalEquipmentToExportDataReader")
                .repository(physicalEquipmentRepository)
                .methodName("findByInventoryId")
                .arguments(inventoryId)
                .pageSize(pageSize)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }

    /**
     * PhysicalEquipment FlatFileItemWriter definition.
     *
     * @param localWorkingFolder generated local working folder
     * @param fileInfo           to get file's information tool.
     * @return the configured writer.
     */
    @Bean
    @StepScope
    public FlatFileItemWriter<PhysicalEquipment> extractedPhysicalEquipmentToExportWriter(
            @Value("#{jobParameters['local.working.folder']}") final String localWorkingFolder,
            final FileMapperInfo fileInfo) {
        final String[] headers = fileInfo.getMapping(FileType.EQUIPEMENT_PHYSIQUE).stream().map(Header::getName).toArray(String[]::new);
        return new FlatFileItemWriterBuilder<PhysicalEquipment>()
                .name("extractedPhysicalEquipmentWriter")
                .resource(new FileSystemResource(Path.of(localWorkingFolder, "physical_equipment.csv")))
                .delimited()
                .delimiter(";")
                .names(headers)
                .headerCallback(writer -> writer.write(String.join(";", headers)))
                .shouldDeleteIfEmpty(true)
                .build();
    }
}
