/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apibatchloading.steps.application.config;

import com.soprasteria.g4it.backend.apiinventory.modeldb.Application;
import com.soprasteria.g4it.backend.apiinventory.repository.ApplicationRepository;
import com.soprasteria.g4it.backend.common.filesystem.model.FileMapperInfo;
import com.soprasteria.g4it.backend.common.filesystem.model.FileType;
import com.soprasteria.g4it.backend.common.filesystem.model.Header;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * Write validated application file Step configuration.
 */
@Configuration
public class WriteValidatedApplicationStepConfiguration {

    /**
     * The step name.
     */
    public static final String WRITE_VALIDATED_APPLICATION_STEP_NAME = "writeValidatedApplicationDataStep";

    /**
     * Step definition.
     *
     * @param jobRepository                  Spring Batch Job Repository.
     * @param transactionManager             the transaction manager (since Spring Batch v5).
     * @param validatedApplicationReader     the validated application data reader.
     * @param validatedApplicationItemWriter the validated application data writer.
     * @param chunkValue                     chunk size.
     * @return the configured Step.
     */
    @Bean
    public Step writeValidatedApplicationDataStep(final JobRepository jobRepository,
                                                  final PlatformTransactionManager transactionManager,
                                                  final RepositoryItemReader<Application> validatedApplicationReader,
                                                  final FlatFileItemWriter<Application> validatedApplicationItemWriter,
                                                  @Value("${loading.batch.chunk}") final Integer chunkValue) {
        return new StepBuilder(WRITE_VALIDATED_APPLICATION_STEP_NAME, jobRepository)
                .<Application, Application>chunk(chunkValue, transactionManager)
                .reader(validatedApplicationReader)
                .writer(validatedApplicationItemWriter)
                .taskExecutor(writeValidApplicationEquipmentTaskExecutor())
                .build();
    }

    /**
     * Task executor.
     *
     * @return the simpleAsyncTaskExecutor
     */
    @Bean
    public TaskExecutor writeValidApplicationEquipmentTaskExecutor() {
        return new SimpleAsyncTaskExecutor("writeValidApplicationEquipmentTaskExecutor");
    }

    /**
     * Validated application data RepositoryItemReader definition.
     *
     * @param applicationRepository the repository to access application data.
     * @param sessionDate           the identifying session of the batch.
     * @param pageSize              the repository reader page size.
     * @return the configured reader.
     */
    @Bean
    @StepScope
    public RepositoryItemReader<Application> validatedApplicationReader(final ApplicationRepository applicationRepository,
                                                                        @Value("#{jobParameters['session.date']}") final Date sessionDate,
                                                                        @Value("${loading.batch.page-size}") final Integer pageSize) {
        final RepositoryItemReaderBuilder<Application> builder = new RepositoryItemReaderBuilder<>();
        return builder
                .name("validatedApplicationReader")
                .repository(applicationRepository)
                .methodName("findBySessionDate")
                .arguments(sessionDate)
                .pageSize(pageSize)
                .sorts(Map.of("lineNumber", Sort.Direction.ASC))
                .build();
    }

    /**
     * Validated application FlatFileItemWriter definition.
     *
     * @param sessionPath        the formatted session date.
     * @param localWorkingFolder generated local working folder
     * @param fileInfo           to get file's information tool.
     * @return the configured writer.
     */
    @Bean
    @StepScope
    public FlatFileItemWriter<Application> validatedApplicationItemWriter(
            @Value("#{jobExecutionContext['session.path']}") final String sessionPath,
            @Value("#{jobParameters['local.working.folder']}") final String localWorkingFolder,
            final FileMapperInfo fileInfo) {
        final List<String> headerList = new ArrayList<>(List.copyOf(fileInfo.getMapping(FileType.APPLICATION).stream().map(Header::getName).toList()));
        // Add 'nomEquipementPhysique to the headers.
        headerList.add("nomEquipementPhysique");
        final String[] headers = headerList.toArray(String[]::new);
        return new FlatFileItemWriterBuilder<Application>()
                .name("validatedApplicationItemWriter")
                .resource(new FileSystemResource(Path.of(localWorkingFolder, String.join("", "accepted_application_", sessionPath, ".csv"))))
                .delimited().delimiter(";")
                .names(headers)
                .headerCallback(writer -> writer.write(String.join(";", headers)))
                .build();
    }
}
