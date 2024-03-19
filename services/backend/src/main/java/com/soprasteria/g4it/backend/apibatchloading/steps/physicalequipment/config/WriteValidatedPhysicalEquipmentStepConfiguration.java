/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apibatchloading.steps.physicalequipment.config;

import com.soprasteria.g4it.backend.apiinventory.modeldb.PhysicalEquipment;
import com.soprasteria.g4it.backend.apiinventory.repository.PhysicalEquipmentRepository;
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
 * Write validated physical equipment file Step configuration.
 */
@Configuration
public class WriteValidatedPhysicalEquipmentStepConfiguration {

    /**
     * The step name.
     */
    public static final String WRITE_VALIDATED_PHYSICAL_EQUIPMENT_STEP_NAME = "writeValidatedPhysicalEquipmentDataStep";

    /**
     * Step definition.
     *
     * @param jobRepository                        Spring Batch Job Repository.
     * @param transactionManager                   the transaction manager (since Spring Batch v5).
     * @param validatedPhysicalEquipmentReader     the validated physical equipment data reader.
     * @param validatedPhysicalEquipmentItemWriter the validated physical equipment data reader.
     * @param chunkValue                           chunk size.
     * @return the configured Step.
     */
    @Bean
    public Step writeValidatedPhysicalEquipmentDataStep(final JobRepository jobRepository,
                                                        final PlatformTransactionManager transactionManager,
                                                        final RepositoryItemReader<PhysicalEquipment> validatedPhysicalEquipmentReader,
                                                        final FlatFileItemWriter<PhysicalEquipment> validatedPhysicalEquipmentItemWriter,
                                                        @Value("${loading.batch.chunk}") final Integer chunkValue) {
        return new StepBuilder(WRITE_VALIDATED_PHYSICAL_EQUIPMENT_STEP_NAME, jobRepository)
                .<PhysicalEquipment, PhysicalEquipment>chunk(chunkValue, transactionManager)
                .reader(validatedPhysicalEquipmentReader)
                .writer(validatedPhysicalEquipmentItemWriter)
                .taskExecutor(writeValidPhysicalEquipmentTaskExecutor())
                .build();
    }

    /**
     * Task executor.
     *
     * @return the simpleAsyncTaskExecutor
     */
    @Bean
    public TaskExecutor writeValidPhysicalEquipmentTaskExecutor() {
        return new SimpleAsyncTaskExecutor("writeValidPhysicalEquipmentTaskExecutor");
    }

    /**
     * Validated physical equipment data RepositoryItemReader definition.
     *
     * @param physicalEquipmentRepository the repository to access physical equipment data.
     * @param sessionDate                 the identifying session of the batch.
     * @param pageSize                    the repository reader page size.
     * @return the configured reader.
     */
    @Bean
    @StepScope
    public RepositoryItemReader<PhysicalEquipment> validatedPhysicalEquipmentReader(final PhysicalEquipmentRepository physicalEquipmentRepository,
                                                                                    @Value("#{jobParameters['session.date']}") final Date sessionDate,
                                                                                    @Value("${loading.batch.page-size}") final Integer pageSize) {
        final RepositoryItemReaderBuilder<PhysicalEquipment> builder = new RepositoryItemReaderBuilder<>();
        return builder
                .name("validatedPhysicalEquipmentReader")
                .repository(physicalEquipmentRepository)
                .methodName("findBySessionDate")
                .arguments(sessionDate)
                .pageSize(pageSize)
                .sorts(Map.of("lineNumber", Sort.Direction.ASC))
                .build();
    }

    /**
     * Validated physical equipment FlatFileItemWriter definition.
     *
     * @param sessionPath        the formatted session date.
     * @param localWorkingFolder generated local working folder
     * @param fileInfo           to get file's information tool.
     * @return the configured writer.
     */
    @Bean
    @StepScope
    public FlatFileItemWriter<PhysicalEquipment> validatedPhysicalEquipmentItemWriter(
            @Value("#{jobExecutionContext['session.path']}") final String sessionPath,
            @Value("#{jobParameters['local.working.folder']}") final String localWorkingFolder,
            final FileMapperInfo fileInfo) {
        final String[] headers = fileInfo.getMapping(FileType.EQUIPEMENT_PHYSIQUE).stream().map(Header::getName).toArray(String[]::new);
        return new FlatFileItemWriterBuilder<PhysicalEquipment>()
                .name("validatedPhysicalEquipmentItemWriter")
                .resource(new FileSystemResource(Path.of(localWorkingFolder, String.join("", "accepted_physical_equipment_", sessionPath, ".csv"))))
                .delimited().delimiter(";").names(headers)
                .headerCallback(writer -> writer.write(String.join(";", headers)))
                .build();
    }
}
