/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apibatchexport.config.step;

import com.soprasteria.g4it.backend.apibatchexport.processor.IndicatorProcessor;
import com.soprasteria.g4it.backend.apiindicator.modeldb.numecoeval.ApplicationIndicator;
import com.soprasteria.g4it.backend.apiindicator.repository.numecoeval.ApplicationIndicatorRepository;
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
 * The Application Indicator extraction Step Configuration.
 */
@Configuration
public class ApplicationIndicatorToExportExtractionStepConfiguration {

    @Bean
    public Flow applicationIndicatorToExportExtractionFlow(final Step applicationIndicatorToExportExtractionStep) {
        return new FlowBuilder<SimpleFlow>("applicationIndicatorToExportExtractionFlow")
                .start(applicationIndicatorToExportExtractionStep)
                .build();
    }

    /**
     * Application Indicator extraction Step configuration.
     *
     * @param jobRepository                                 Spring Batch Job Repository.
     * @param transactionManager                            the transaction manager (since Spring Batch v5).
     * @param extractApplicationIndicatorToExportDataReader RepositoryItemReader to extract Application Indicator data from database.
     * @param applicationIndicatorIndicatorProcessor        the processor.
     * @param extractApplicationIndicatorToExportWriter     FlatFileItemReader to write extraction file.
     * @param chunkValue                                    chunk size.
     * @return the configured Step.
     */
    @Bean
    public Step applicationIndicatorToExportExtractionStep(final JobRepository jobRepository,
                                                           final PlatformTransactionManager transactionManager,
                                                           final RepositoryItemReader<ApplicationIndicator> extractApplicationIndicatorToExportDataReader,
                                                           final IndicatorProcessor<ApplicationIndicator> applicationIndicatorIndicatorProcessor,
                                                           final FlatFileItemWriter<ApplicationIndicator> extractApplicationIndicatorToExportWriter,
                                                           @Value("${export.batch.chunk}") final Integer chunkValue) {
        return new StepBuilder("applicationIndicatorToExportExtractionStep", jobRepository)
                .<ApplicationIndicator, ApplicationIndicator>chunk(chunkValue, transactionManager)
                .reader(extractApplicationIndicatorToExportDataReader)
                .processor(applicationIndicatorIndicatorProcessor)
                .writer(extractApplicationIndicatorToExportWriter)
                .taskExecutor(applicationIndicatorToExportTaskExecutor())
                .build();
    }

    /**
     * Task executor.
     *
     * @return the simpleAsyncTaskExecutor
     */
    @Bean
    public TaskExecutor applicationIndicatorToExportTaskExecutor() {
        return new SimpleAsyncTaskExecutor("applicationIndicatorToExportTaskExecutor");
    }

    /**
     * RepositoryItemReader to extract Application Indicator data from database.
     *
     * @param applicationIndicatorRepository the repository to access Application Indicator data from DataBase.
     * @param batchName                      the linked batch name.
     * @param pageSize                       the repository reader page size.
     * @return the configured reader.
     */
    @Bean
    @StepScope
    public RepositoryItemReader<ApplicationIndicator> extractApplicationIndicatorToExportDataReader(final ApplicationIndicatorRepository applicationIndicatorRepository,
                                                                                                    @Value("#{jobParameters['batch.name']}") final String batchName,
                                                                                                    @Value("${export.batch.page-size}") final Integer pageSize) {
        final RepositoryItemReaderBuilder<ApplicationIndicator> builder = new RepositoryItemReaderBuilder<>();
        return builder.name("extractApplicationIndicatorToExportWriter")
                .repository(applicationIndicatorRepository)
                .methodName("findByBatchName")
                .arguments(batchName)
                .pageSize(pageSize)
                .sorts(Map.of("batchName", Sort.Direction.ASC))
                .build();
    }

    /**
     * The indicator processor.
     *
     * @param inventoryName  the inventory name to process.
     * @param sipReferential the sip referential data.
     * @return the configured processor.
     */
    @Bean
    @StepScope
    public IndicatorProcessor<ApplicationIndicator> applicationIndicatorIndicatorProcessor(
            @Value("#{jobParameters['inventory.name']}") final String inventoryName,
            @Value("#{jobExecutionContext['" + LoadSipReferentialStepConfiguration.SIP_CONTEXT_KEY + "']}") final Map<String, Double> sipReferential) {
        return new IndicatorProcessor<>(inventoryName, sipReferential);
    }

    /**
     * Application Indicator FlatFileItemWriter definition.
     *
     * @param localWorkingFolder generated local working folder
     * @param fileInfo           to get file's information tool.
     * @return the configured writer.
     */
    @Bean
    @StepScope
    public FlatFileItemWriter<ApplicationIndicator> extractApplicationIndicatorToExportWriter(
            @Value("#{jobParameters['local.working.folder']}") final String localWorkingFolder,
            final FileMapperInfo fileInfo) {
        final String[] headers = fileInfo.getMapping(FileType.APPLICATION_INDICATOR).stream().map(Header::getName).toArray(String[]::new);
        return new FlatFileItemWriterBuilder<ApplicationIndicator>()
                .name("extractApplicationIndicatorToExportWriter")
                .resource(new FileSystemResource(Path.of(localWorkingFolder, "ind_application.csv")))
                .delimited()
                .delimiter(";")
                .names(headers)
                .headerCallback(writer -> writer.write(String.join(";", headers)))
                .shouldDeleteIfEmpty(true)
                .build();
    }
}
