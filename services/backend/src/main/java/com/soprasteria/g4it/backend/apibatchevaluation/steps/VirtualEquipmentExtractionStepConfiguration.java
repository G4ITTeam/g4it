/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apibatchevaluation.steps;

import com.soprasteria.g4it.backend.apiinventory.modeldb.VirtualEquipment;
import com.soprasteria.g4it.backend.apiinventory.repository.VirtualEquipmentRepository;
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
 * The VirtualEquipment extraction Step Configuration.
 */
@Configuration
public class VirtualEquipmentExtractionStepConfiguration {

    @Bean
    public Flow virtualEquipmentExtractionFlow(final Step virtualEquipmentExtractionStep) {
        return new FlowBuilder<SimpleFlow>("virtualEquipmentExtractionFlow")
                .start(virtualEquipmentExtractionStep)
                .build();
    }

    /**
     * VirtualEquipment extraction Step configuration.
     *
     * @param jobRepository                     Spring Batch Job Repository.
     * @param transactionManager                the transaction manager (since Spring Batch v5).
     * @param extractVirtualEquipmentDataReader RepositoryItemReader to extract VirtualEquipment data from database.
     * @param extractedVirtualEquipmentWriter   FlatFileItemReader to write extraction file.
     * @param chunkValue                        chunk size.
     * @return the configured Step.
     */
    @Bean
    public Step virtualEquipmentExtractionStep(final JobRepository jobRepository,
                                               final PlatformTransactionManager transactionManager,
                                               final RepositoryItemReader<VirtualEquipment> extractVirtualEquipmentDataReader,
                                               final FlatFileItemWriter<VirtualEquipment> extractedVirtualEquipmentWriter,
                                               @Value("${evaluation.batch.chunk}") final Integer chunkValue) {
        return new StepBuilder("virtualEquipmentExtractionStep", jobRepository)
                .<VirtualEquipment, VirtualEquipment>chunk(chunkValue, transactionManager)
                .reader(extractVirtualEquipmentDataReader)
                .writer(extractedVirtualEquipmentWriter)
                .taskExecutor(virtualEquipmentTaskExecutor())
                .build();
    }

    /**
     * Task executor.
     *
     * @return the simpleAsyncTaskExecutor
     */
    @Bean
    public TaskExecutor virtualEquipmentTaskExecutor() {
        return new SimpleAsyncTaskExecutor("virtualEquipmentTaskExecutor");
    }

    /**
     * RepositoryItemReader to extract VirtualEquipment data from database.
     *
     * @param virtualEquipmentRepository the repository to access VirtualEquipment data from DataBase.
     * @param inventoryId                the inventory Id.
     * @param pageSize                   the repository reader page size.
     * @return the configured reader.
     */
    @Bean
    @StepScope
    public RepositoryItemReader<VirtualEquipment> extractVirtualEquipmentDataReader(final VirtualEquipmentRepository virtualEquipmentRepository,
                                                                                    @Value("#{jobParameters['inventory.id']}") final long inventoryId,
                                                                                    @Value("${evaluation.batch.page-size}") final Integer pageSize) {
        final RepositoryItemReaderBuilder<VirtualEquipment> builder = new RepositoryItemReaderBuilder<>();
        return builder.name("extractVirtualEquipmentDataReader")
                .repository(virtualEquipmentRepository)
                .methodName("findByInventoryId")
                .arguments(inventoryId)
                .pageSize(pageSize)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }

    /**
     * VirtualEquipment FlatFileItemWriter definition.
     *
     * @param localWorkingFolder generated local working folder
     * @param fileInfo           to get file's information tool.
     * @return the configured writer.
     */
    @Bean
    @StepScope
    public FlatFileItemWriter<VirtualEquipment> extractedVirtualEquipmentWriter(
            @Value("#{jobParameters['local.working.folder']}") final String localWorkingFolder,
            final FileMapperInfo fileInfo) {
        final String[] headers = fileInfo.getMapping(FileType.EQUIPEMENT_VIRTUEL).stream().map(Header::getName).toArray(String[]::new);
        return new FlatFileItemWriterBuilder<VirtualEquipment>()
                .name("extractedVirtualEquipmentWriter")
                .resource(new FileSystemResource(Path.of(localWorkingFolder, "virtual_equipment.csv")))
                .delimited()
                .delimiter(";")
                .names(headers)
                .headerCallback(writer -> writer.write(String.join(";", headers)))
                .shouldDeleteIfEmpty(true)
                .build();
    }
}
