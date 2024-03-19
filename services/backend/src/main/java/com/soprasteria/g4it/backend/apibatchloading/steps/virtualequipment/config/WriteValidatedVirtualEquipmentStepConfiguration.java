/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apibatchloading.steps.virtualequipment.config;

import com.soprasteria.g4it.backend.apiinventory.modeldb.VirtualEquipment;
import com.soprasteria.g4it.backend.apiinventory.repository.VirtualEquipmentRepository;
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
 * Write validated virtual equipment file Step configuration.
 */
@Configuration
public class WriteValidatedVirtualEquipmentStepConfiguration {

    public static final String WRITE_VALIDATED_VIRTUAL_EQUIPMENT_STEP_NAME = "writeValidatedVirtualEquipmentDataStep";

    /**
     * Step definition.
     *
     * @param jobRepository                       Spring Batch Job Repository.
     * @param transactionManager                  the transaction manager (since Spring Batch v5).
     * @param validatedVirtualEquipmentReader     the validated virtual equipment data reader.
     * @param validatedVirtualEquipmentItemWriter the validated virtual equipment data reader.
     * @param chunkValue                          chunk size.
     * @return the configured Step.
     */
    @Bean
    public Step writeValidatedVirtualEquipmentDataStep(final JobRepository jobRepository,
                                                       final PlatformTransactionManager transactionManager,
                                                       final RepositoryItemReader<VirtualEquipment> validatedVirtualEquipmentReader,
                                                       final FlatFileItemWriter<VirtualEquipment> validatedVirtualEquipmentItemWriter,
                                                       @Value("${loading.batch.chunk}") final Integer chunkValue) {
        return new StepBuilder(WRITE_VALIDATED_VIRTUAL_EQUIPMENT_STEP_NAME, jobRepository)
                .<VirtualEquipment, VirtualEquipment>chunk(chunkValue, transactionManager)
                .reader(validatedVirtualEquipmentReader)
                .writer(validatedVirtualEquipmentItemWriter)
                .taskExecutor(writeValidVirtualEquipmentTaskExecutor())
                .build();
    }

    /**
     * Task executor.
     *
     * @return the simpleAsyncTaskExecutor
     */
    @Bean
    public TaskExecutor writeValidVirtualEquipmentTaskExecutor() {
        return new SimpleAsyncTaskExecutor("writeValidVirtualEquipmentTaskExecutor");
    }

    /**
     * Validated virtual equipment data RepositoryItemReader definition.
     *
     * @param virtualEquipmentRepository the repository to access virtual equipment data.
     * @param sessionDate                the identifying session of the batch.
     * @param pageSize                   the repository reader page size.
     * @return the configured reader.
     */
    @Bean
    @StepScope
    public RepositoryItemReader<VirtualEquipment> validatedVirtualEquipmentReader(final VirtualEquipmentRepository virtualEquipmentRepository,
                                                                                  @Value("#{jobParameters['session.date']}") final Date sessionDate,
                                                                                  @Value("${loading.batch.page-size}") final Integer pageSize) {
        final RepositoryItemReaderBuilder<VirtualEquipment> builder = new RepositoryItemReaderBuilder<>();
        return builder.name("validatedVirtualEquipmentReader")
                .repository(virtualEquipmentRepository)
                .methodName("findBySessionDate")
                .arguments(sessionDate)
                .pageSize(pageSize)
                .sorts(Map.of("lineNumber", Sort.Direction.ASC))
                .build();
    }

    /**
     * Validated virtual equipment FlatFileItemWriter definition.
     *
     * @param sessionPath        the formatted session date.
     * @param localWorkingFolder generated local working folder
     * @param fileInfo           to get file's information tool.
     * @return the configured writer.
     */
    @Bean
    @StepScope
    public FlatFileItemWriter<VirtualEquipment> validatedVirtualEquipmentItemWriter(
            @Value("#{jobExecutionContext['session.path']}") final String sessionPath,
            @Value("#{jobParameters['local.working.folder']}") final String localWorkingFolder,
            final FileMapperInfo fileInfo) {
        final String[] headers = fileInfo.getMapping(FileType.EQUIPEMENT_VIRTUEL).stream().map(Header::getName).toArray(String[]::new);
        return new FlatFileItemWriterBuilder<VirtualEquipment>()
                .name("validatedVirtualEquipmentItemWriter")
                .resource(new FileSystemResource(Path.of(localWorkingFolder, String.join("", "accepted_virtual_equipment_", sessionPath, ".csv"))))
                .delimited().delimiter(";").names(headers)
                .headerCallback(writer -> writer.write(String.join(";", headers)))
                .build();
    }
}
