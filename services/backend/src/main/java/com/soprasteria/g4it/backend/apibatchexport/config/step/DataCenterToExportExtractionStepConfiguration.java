/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apibatchexport.config.step;

import com.soprasteria.g4it.backend.apiinventory.modeldb.DataCenter;
import com.soprasteria.g4it.backend.apiinventory.repository.DataCenterRepository;
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
 * The DataCenter extraction Step Configuration.
 */
@Configuration
public class DataCenterToExportExtractionStepConfiguration {

    @Bean
    public Flow dataCenterToExportExtractionFlow(final Step dataCenterToExportExtractionStep) {
        return new FlowBuilder<SimpleFlow>("dataCenterToExportExtractionFlow")
                .start(dataCenterToExportExtractionStep)
                .build();
    }

    /**
     * DataCenter extraction Step configuration.
     *
     * @param jobRepository                       Spring Batch Job Repository.
     * @param transactionManager                  the transaction manager (since Spring Batch v5).
     * @param extractDataCenterToExportDataReader RepositoryItemReader to extract DataCenter data from database.
     * @param extractedDataCenterToExportWriter   FlatFileItemReader to write extraction file.
     * @param chunkValue                          chunk size.
     * @return the configured Step.
     */
    @Bean
    public Step dataCenterToExportExtractionStep(final JobRepository jobRepository,
                                                 final PlatformTransactionManager transactionManager,
                                                 final RepositoryItemReader<DataCenter> extractDataCenterToExportDataReader,
                                                 final FlatFileItemWriter<DataCenter> extractedDataCenterToExportWriter,
                                                 @Value("${export.batch.chunk}") final Integer chunkValue) {
        return new StepBuilder("dataCenterToExportExtractionStep", jobRepository)
                .<DataCenter, DataCenter>chunk(chunkValue, transactionManager)
                .reader(extractDataCenterToExportDataReader)
                .writer(extractedDataCenterToExportWriter)
                .taskExecutor(dataCenterToExportTaskExecutor())
                .build();
    }

    /**
     * Task executor.
     *
     * @return the simpleAsyncTaskExecutor
     */
    @Bean
    public TaskExecutor dataCenterToExportTaskExecutor() {
        return new SimpleAsyncTaskExecutor("dataCenterToExportTaskExecutor");
    }

    /**
     * RepositoryItemReader to extract valid DataCenter data from database.
     *
     * @param dataCenterRepository the repository to access DataCenter data from DataBase.
     * @param inventoryId          the inventory id.
     * @param pageSize             the repository reader page size.
     * @return the configured reader.
     */
    @Bean
    @StepScope
    public RepositoryItemReader<DataCenter> extractDataCenterToExportDataReader(final DataCenterRepository dataCenterRepository,
                                                                                @Value("#{jobParameters['inventory.id']}") final long inventoryId,
                                                                                @Value("${export.batch.page-size}") final Integer pageSize) {
        final RepositoryItemReaderBuilder<DataCenter> builder = new RepositoryItemReaderBuilder<>();
        return builder.name("extractDataCenterToExportDataReader")
                .repository(dataCenterRepository)
                .methodName("findByInventoryId")
                .arguments(inventoryId)
                .pageSize(pageSize)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }

    /**
     * DataCenter FlatFileItemWriter definition.
     *
     * @param localWorkingFolder generated local working folder
     * @param fileInfo           to get file's information tool.
     * @return the configured writer.
     */
    @Bean
    @StepScope
    public FlatFileItemWriter<DataCenter> extractedDataCenterToExportWriter(
            @Value("#{jobParameters['local.working.folder']}") final String localWorkingFolder,
            final FileMapperInfo fileInfo) {
        final String[] headers = fileInfo.getMapping(FileType.DATACENTER).stream().map(Header::getName).toArray(String[]::new);
        return new FlatFileItemWriterBuilder<DataCenter>()
                .name("extractedDataCenterToExportWriter")
                .resource(new FileSystemResource(Path.of(localWorkingFolder, "datacenter.csv")))
                .delimited()
                .delimiter(";")
                .names(headers)
                .headerCallback(writer -> writer.write(String.join(";", headers)))
                .shouldDeleteIfEmpty(true)
                .build();
    }
}
