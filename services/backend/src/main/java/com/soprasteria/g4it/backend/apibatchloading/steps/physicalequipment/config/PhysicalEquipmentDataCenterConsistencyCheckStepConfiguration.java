/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apibatchloading.steps.physicalequipment.config;

import com.soprasteria.g4it.backend.apibatchloading.steps.physicalequipment.processor.PhysicalEquipmentDataCenterConsistencyCheckProcessor;
import com.soprasteria.g4it.backend.apiinventory.modeldb.PhysicalEquipment;
import com.soprasteria.g4it.backend.apiinventory.repository.PhysicalEquipmentRepository;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * Physical Equipment - DataCenter Consistency check Step configuration.
 */
@Configuration
public class PhysicalEquipmentDataCenterConsistencyCheckStepConfiguration {

    /**
     * The step name.
     */
    public static final String PHYSICAL_EQUIPMENT_DATA_CENTER_CONSISTENCY_CHECK_STEP_NAME = "physicalEquipmentDataCenterConsistencyCheckStep";

    /**
     * Step definition.
     *
     * @param jobRepository                                               Spring Batch Job Repository.
     * @param transactionManager                                          the transaction manager (since Spring Batch v5).     * @param physicalEquipmentNotLinkedToDataCenterReader                the validated physical equipment data reader.
     * @param physicalEquipmentDataCenterConsistencyCheckProcessor        the consistency check processor.
     * @param unvalidatedPhysicalEquipmentAfterConsistencyCheckItemWriter the ItemWriter to append in reject file.
     * @return the configured Step.
     */
    @Bean
    public Step physicalEquipmentDataCenterConsistencyCheckStep(final JobRepository jobRepository,
                                                                final PlatformTransactionManager transactionManager,
                                                                final RepositoryItemReader<PhysicalEquipment> physicalEquipmentNotLinkedToDataCenterReader,
                                                                final PhysicalEquipmentDataCenterConsistencyCheckProcessor physicalEquipmentDataCenterConsistencyCheckProcessor,
                                                                final FlatFileItemWriter<PhysicalEquipment> unvalidatedPhysicalEquipmentAfterConsistencyCheckItemWriter) {
        return new StepBuilder(PHYSICAL_EQUIPMENT_DATA_CENTER_CONSISTENCY_CHECK_STEP_NAME, jobRepository)
                .<PhysicalEquipment, PhysicalEquipment>chunk(10, transactionManager)
                .reader(physicalEquipmentNotLinkedToDataCenterReader)
                .processor(physicalEquipmentDataCenterConsistencyCheckProcessor)
                .writer(unvalidatedPhysicalEquipmentAfterConsistencyCheckItemWriter)
                .taskExecutor(consistencyToDataCenterTaskExecutor())
                .build();
    }

    /**
     * Task executor.
     *
     * @return the simpleAsyncTaskExecutor
     */
    @Bean
    public TaskExecutor consistencyToDataCenterTaskExecutor() {
        return new SimpleAsyncTaskExecutor("consistencyToDataCenterTaskExecutor");
    }


    /**
     * Consistency check RepositoryItemReader definition.
     *
     * @param physicalEquipmentRepository the repository to access physical equipment data.
     * @param sessionDate                 the identifying session of the batch
     * @param pageSize                    the repository reader page size.
     * @return the reader configured.
     */
    @Bean
    @StepScope
    public RepositoryItemReader<PhysicalEquipment> physicalEquipmentNotLinkedToDataCenterReader(final PhysicalEquipmentRepository physicalEquipmentRepository,
                                                                                                @Value("#{jobParameters['session.date']}") final Date sessionDate,
                                                                                                @Value("${loading.batch.page-size}") final Integer pageSize) {
        final RepositoryItemReaderBuilder<PhysicalEquipment> builder = new RepositoryItemReaderBuilder<>();
        return builder.name("physicalEquipmentNotLinkedToDataCenterReader")
                .repository(physicalEquipmentRepository)
                .methodName("findPhysicalEquipmentNotLinkedToDataCenter")
                .arguments(sessionDate)
                .pageSize(pageSize)
                .sorts(Map.of("lineNumber", Sort.Direction.ASC))
                .build();
    }

    /**
     * Consistency check processor.
     *
     * @param locale        the current locale.
     * @param messageSource the internalisation message.
     * @return the processor configured.
     */
    @Bean
    @StepScope
    public PhysicalEquipmentDataCenterConsistencyCheckProcessor physicalEquipmentDataCenterConsistencyCheckProcessor(@Value("#{jobParameters['locale']}") final Locale locale,
                                                                                                                     final MessageSource messageSource) {
        return new PhysicalEquipmentDataCenterConsistencyCheckProcessor(messageSource, locale);
    }

    /**
     * Consistency Check FlatFileItemWriter
     *
     * @param unvalidatedPhysicalEquipmentItemWriter writer defined in FlagStepConfiguration.
     * @return the same writer defined in FlagStepConfiguration, except that it allows append.
     */
    @Bean
    @StepScope
    public FlatFileItemWriter<PhysicalEquipment> unvalidatedPhysicalEquipmentAfterConsistencyCheckItemWriter(final FlatFileItemWriter<PhysicalEquipment> unvalidatedPhysicalEquipmentItemWriter) {
        unvalidatedPhysicalEquipmentItemWriter.setAppendAllowed(true);
        return unvalidatedPhysicalEquipmentItemWriter;
    }

}
