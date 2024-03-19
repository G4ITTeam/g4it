/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apibatchexport.config.step;

import com.soprasteria.g4it.backend.apibatchexport.processor.IndicatorProcessor;
import com.soprasteria.g4it.backend.apiindicator.modeldb.numecoeval.VirtualEquipmentIndicator;
import com.soprasteria.g4it.backend.apiindicator.repository.numecoeval.VirtualEquipmentIndicatorRepository;
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
 * The Virtual Equipment Indicator extraction Step Configuration.
 */
@Configuration
public class VirtualEquipmentIndicatorToExportExtractionStepConfiguration {

    @Bean
    public Flow virtualEquipmentIndicatorToExportExtractionFlow(final Step virtualEquipmentIndicatorToExportExtractionStep) {
        return new FlowBuilder<SimpleFlow>("virtualEquipmentIndicatorToExportExtractionFlow")
                .start(virtualEquipmentIndicatorToExportExtractionStep)
                .build();
    }

    /**
     * Virtual Equipment Indicator extraction Step configuration.
     *
     * @param jobRepository                                      Spring Batch Job Repository.
     * @param transactionManager                                 the transaction manager (since Spring Batch v5).
     * @param extractVirtualEquipmentIndicatorToExportDataReader RepositoryItemReader to extract Virtual Equipment Indicator data from database.
     * @param virtualEquipmentIndicatorIndicatorProcessor        the processor.
     * @param extractVirtualEquipmentIndicatorToExportWriter     FlatFileItemReader to write extraction file.
     * @param chunkValue                                         chunk size.
     * @return the configured Step.
     */
    @Bean
    public Step virtualEquipmentIndicatorToExportExtractionStep(final JobRepository jobRepository,
                                                                final PlatformTransactionManager transactionManager,
                                                                final RepositoryItemReader<VirtualEquipmentIndicator> extractVirtualEquipmentIndicatorToExportDataReader,
                                                                final IndicatorProcessor<VirtualEquipmentIndicator> virtualEquipmentIndicatorIndicatorProcessor,
                                                                final FlatFileItemWriter<VirtualEquipmentIndicator> extractVirtualEquipmentIndicatorToExportWriter,
                                                                @Value("${export.batch.chunk}") final Integer chunkValue) {
        return new StepBuilder("virtualEquipmentIndicatorToExportExtractionStep", jobRepository)
                .<VirtualEquipmentIndicator, VirtualEquipmentIndicator>chunk(chunkValue, transactionManager)
                .reader(extractVirtualEquipmentIndicatorToExportDataReader)
                .processor(virtualEquipmentIndicatorIndicatorProcessor)
                .writer(extractVirtualEquipmentIndicatorToExportWriter)
                .taskExecutor(virtualEquipmentIndicatorToExportTaskExecutor())
                .build();
    }

    /**
     * Task executor.
     *
     * @return the simpleAsyncTaskExecutor
     */
    @Bean
    public TaskExecutor virtualEquipmentIndicatorToExportTaskExecutor() {
        return new SimpleAsyncTaskExecutor("virtualEquipmentIndicatorToExportTaskExecutor");
    }

    /**
     * RepositoryItemReader to extract Virtual Equipment Indicator data from database.
     *
     * @param virtualEquipmentIndicatorRepository the repository to access Virtual Equipment Indicator data from DataBase.
     * @param batchName                           the linked batch name.
     * @param pageSize                            the repository reader page size.
     * @return the configured reader.
     */
    @Bean
    @StepScope
    public RepositoryItemReader<VirtualEquipmentIndicator> extractVirtualEquipmentIndicatorToExportDataReader(final VirtualEquipmentIndicatorRepository virtualEquipmentIndicatorRepository,
                                                                                                              @Value("#{jobParameters['batch.name']}") final String batchName,
                                                                                                              @Value("${export.batch.page-size}") final Integer pageSize) {
        final RepositoryItemReaderBuilder<VirtualEquipmentIndicator> builder = new RepositoryItemReaderBuilder<>();
        return builder.name("extractVirtualEquipmentIndicatorToExportDataReader")
                .repository(virtualEquipmentIndicatorRepository)
                .methodName("findByBatchName")
                .arguments(batchName)
                .pageSize(pageSize)
                .sorts(Map.of("batchName", Sort.Direction.ASC))
                .build();
    }

    /**
     * The indicator processor.
     *
     * @param inventoryName    the inventory name to process.
     * @param sipReferential the sip referential data.
     * @return the configured processor.
     */
    @Bean
    @StepScope
    public IndicatorProcessor<VirtualEquipmentIndicator> virtualEquipmentIndicatorIndicatorProcessor(
            @Value("#{jobParameters['inventory.name']}") final String inventoryName,
            @Value("#{jobExecutionContext['" + LoadSipReferentialStepConfiguration.SIP_CONTEXT_KEY + "']}") final Map<String, Double> sipReferential) {
        return new IndicatorProcessor<>(inventoryName, sipReferential);
    }

    /**
     * Virtual Equipment Indicator FlatFileItemWriter definition.
     *
     * @param localWorkingFolder generated local working folder
     * @param fileInfo           to get file's information tool.
     * @return the configured writer.
     */
    @Bean
    @StepScope
    public FlatFileItemWriter<VirtualEquipmentIndicator> extractedVirtualEquipmentIndicatorToExportWriter(
            @Value("#{jobParameters['local.working.folder']}") final String localWorkingFolder,
            final FileMapperInfo fileInfo) {
        final String[] headers = fileInfo.getMapping(FileType.VIRTUAL_EQUIPMENT_INDICATOR).stream().map(Header::getName).toArray(String[]::new);
        return new FlatFileItemWriterBuilder<VirtualEquipmentIndicator>()
                .name("extractedVirtualEquipmentIndicatorToExportWriter")
                .resource(new FileSystemResource(Path.of(localWorkingFolder, "ind_virtual_equipment.csv")))
                .delimited()
                .delimiter(";")
                .names(headers)
                .headerCallback(writer -> writer.write(String.join(";", headers)))
                .shouldDeleteIfEmpty(true)
                .build();
    }
}
