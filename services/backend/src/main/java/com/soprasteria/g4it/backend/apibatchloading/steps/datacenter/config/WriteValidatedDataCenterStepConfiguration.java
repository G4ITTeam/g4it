/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apibatchloading.steps.datacenter.config;

import com.soprasteria.g4it.backend.apiinventory.modeldb.DataCenter;
import com.soprasteria.g4it.backend.apiinventory.repository.DataCenterRepository;
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
import java.util.Date;
import java.util.Map;

/**
 * Write valid datacenter file step configuration.
 */
@Configuration
public class WriteValidatedDataCenterStepConfiguration {

    /**
     * The step name.
     */
    public static final String WRITE_VALIDATED_DATACENTER_STEP_NAME = "writeValidatedDataCenterDataStep";

    /**
     * Step definition.
     *
     * @param jobRepository                 Spring Batch Job Repository.
     * @param transactionManager            the transaction manager (since Spring Batch v5).
     * @param validatedDataCenterReader     the validated datacenter data reader.
     * @param validatedDataCenterItemWriter the validated datacenter data writer.
     * @param chunkValue                    chunk size.
     * @return the configured Step.
     */
    @Bean
    public Step writeValidatedDataCenterDataStep(final JobRepository jobRepository,
                                                 final PlatformTransactionManager transactionManager,
                                                 final RepositoryItemReader<DataCenter> validatedDataCenterReader,
                                                 final FlatFileItemWriter<DataCenter> validatedDataCenterItemWriter,
                                                 @Value("${loading.batch.chunk}") final Integer chunkValue) {
        return new StepBuilder(WRITE_VALIDATED_DATACENTER_STEP_NAME, jobRepository)
                .<DataCenter, DataCenter>chunk(chunkValue, transactionManager)
                .reader(validatedDataCenterReader)
                .writer(validatedDataCenterItemWriter)
                .taskExecutor(writeValidDatacenterTaskExecutor())
                .build();
    }

    /**
     * Task executor.
     *
     * @return the simpleAsyncTaskExecutor
     */
    @Bean
    public TaskExecutor writeValidDatacenterTaskExecutor() {
        return new SimpleAsyncTaskExecutor("writeValidDatacenterTaskExecutor");
    }

    /**
     * Validated datacenter data RepositoryItemReader definition.
     *
     * @param dataCenterRepository the repository to access datacenter data.
     * @param sessionDate          the identifying session of the batch.
     * @param pageSize             the repository reader page size.
     * @return the configured reader.
     */
    @Bean
    @StepScope
    public RepositoryItemReader<DataCenter> validatedDataCenterReader(final DataCenterRepository dataCenterRepository,
                                                                      @Value("#{jobParameters['session.date']}") final Date sessionDate,
                                                                      @Value("${loading.batch.page-size}") final Integer pageSize) {
        final RepositoryItemReaderBuilder<DataCenter> builder = new RepositoryItemReaderBuilder<>();
        return builder
                .name("validatedDataCenterReader")
                .repository(dataCenterRepository)
                .methodName("findBySessionDate")
                .arguments(sessionDate)
                .pageSize(pageSize)
                .sorts(Map.of("lineNumber", Sort.Direction.ASC)).build();
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
    public FlatFileItemWriter<DataCenter> validatedDataCenterItemWriter(
            @Value("#{jobExecutionContext['session.path']}") final String sessionPath,
            @Value("#{jobParameters['local.working.folder']}") final String localWorkingFolder,
            final FileMapperInfo fileInfo) {
        final String[] headers = fileInfo.getMapping(FileType.DATACENTER).stream().map(Header::getName).toArray(String[]::new);
        return new FlatFileItemWriterBuilder<DataCenter>()
                .name("validatedDataCenterItemWriter")
                .resource(new FileSystemResource(Path.of(localWorkingFolder, String.join("", "accepted_datacenter_", sessionPath, ".csv"))))
                .delimited().delimiter(";").names(headers)
                .headerCallback(writer -> writer.write(String.join(";", headers)))
                .build();
    }
}
