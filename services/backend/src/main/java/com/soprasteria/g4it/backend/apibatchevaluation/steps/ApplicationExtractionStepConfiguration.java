/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apibatchevaluation.steps;

import com.soprasteria.g4it.backend.apiinventory.modeldb.Application;
import com.soprasteria.g4it.backend.apiinventory.repository.ApplicationRepository;
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
 * The Application extraction Step Configuration.
 */
@Configuration
public class ApplicationExtractionStepConfiguration {

    @Bean
    public Flow applicationExtractionFlow(final Step applicationExtractionStep) {
        return new FlowBuilder<SimpleFlow>("applicationExtractionFlow")
                .start(applicationExtractionStep)
                .build();
    }

    /**
     * Application extraction Step configuration.
     *
     * @param jobRepository                Spring Batch Job Repository.
     * @param transactionManager           the transaction manager (since Spring Batch v5).
     * @param extractApplicationDataReader RepositoryItemReader to extract Application data from database.
     * @param extractedApplicationWriter   FlatFileItemReader to write extraction file.
     * @param chunkValue                   chunk size.
     * @return the configured Step.
     */
    @Bean
    public Step applicationExtractionStep(final JobRepository jobRepository,
                                          final PlatformTransactionManager transactionManager,
                                          final RepositoryItemReader<Application> extractApplicationDataReader,
                                          final FlatFileItemWriter<Application> extractedApplicationWriter,
                                          @Value("${evaluation.batch.chunk}") final Integer chunkValue) {
        return new StepBuilder("applicationExtractionStep", jobRepository)
                .<Application, Application>chunk(chunkValue, transactionManager)
                .reader(extractApplicationDataReader)
                .writer(extractedApplicationWriter)
                .taskExecutor(applicationTaskExecutor())
                .build();
    }

    /**
     * Task executor.
     *
     * @return the simpleAsyncTaskExecutor
     */
    @Bean
    public TaskExecutor applicationTaskExecutor() {
        return new SimpleAsyncTaskExecutor("applicationTaskExecutor");
    }

    /**
     * RepositoryItemReader to extract valid Application data from database.
     *
     * @param applicationRepository the repository to access Application data from DataBase.
     * @param inventoryId           the inventory id.
     * @param pageSize              the repository reader page size.
     * @return the configured reader.
     */
    @Bean
    @StepScope
    public RepositoryItemReader<Application> extractApplicationDataReader(final ApplicationRepository applicationRepository,
                                                                          @Value("#{jobParameters['inventory.id']}") final long inventoryId,
                                                                          @Value("${evaluation.batch.page-size}") final Integer pageSize) {
        final RepositoryItemReaderBuilder<Application> builder = new RepositoryItemReaderBuilder<>();
        return builder.name("extractApplicationDataReader")
                .repository(applicationRepository)
                .methodName("findByInventoryId")
                .arguments(inventoryId)
                .pageSize(pageSize)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }

    /**
     * Application FlatFileItemWriter definition.
     *
     * @param localWorkingFolder generated local working folder
     * @param fileInfo           to get file's information tool.
     * @return the configured writer.
     */
    @Bean
    @StepScope
    public FlatFileItemWriter<Application> extractedApplicationWriter(
            @Value("#{jobParameters['local.working.folder']}") final String localWorkingFolder,
            final FileMapperInfo fileInfo) {
        final String[] headers = fileInfo.getMapping(FileType.APPLICATION).stream().map(Header::getName).toList().toArray(String[]::new);
        return new FlatFileItemWriterBuilder<Application>()
                .name("extractedApplicationWriter")
                .resource(new FileSystemResource(Path.of(localWorkingFolder, "application.csv")))
                .delimited()
                .delimiter(";")
                .names(headers)
                .headerCallback(writer -> writer.write(String.join(";", headers)))
                .shouldDeleteIfEmpty(true)
                .build();
    }
}
