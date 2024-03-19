/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apibatchloading.steps.virtualequipment.config;

import com.soprasteria.g4it.backend.apibatchloading.steps.virtualequipment.processor.VirtualEquipmentPhysicalEquipmentConsistencyCheckProcessor;
import com.soprasteria.g4it.backend.apiinventory.modeldb.VirtualEquipment;
import com.soprasteria.g4it.backend.apiinventory.repository.VirtualEquipmentRepository;
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
 * Virtual Equipment - Physical Consistency check Step configuration.
 */
@Configuration
public class VirtualEquipmentPhysicalEquipmentConsistencyCheckStepConfiguration {

    /**
     * The step name.
     */
    public static final String VIRTUAL_EQUIPMENT_PHYSICAL_EQUIPMENT_CONSISTENCY_CHECK_STEP_NAME = "virtualEquipmentPhysicalEquipmentConsistencyCheckStep";

    /**
     * Step definition.
     *
     * @param jobRepository                                              Spring Batch Job Repository.
     * @param transactionManager                                         the transaction manager (since Spring Batch v5).     * @param virtualEquipmentNotLinkedToPhysicalEquipmentReader         the validated virtual equipment data reader.
     * @param virtualEquipmentPhysicalEquipmentConsistencyCheckProcessor the consistency check processor.
     * @param unvalidatedVirtualEquipmentAfterConsistencyCheckItemWriter the unvalidated file item writer.
     * @return the configured Step.
     */
    @Bean
    public Step virtualEquipmentPhysicalEquipmentConsistencyCheckStep(final JobRepository jobRepository,
                                                                      final PlatformTransactionManager transactionManager,
                                                                      final RepositoryItemReader<VirtualEquipment> virtualEquipmentNotLinkedToPhysicalEquipmentReader,
                                                                      final VirtualEquipmentPhysicalEquipmentConsistencyCheckProcessor virtualEquipmentPhysicalEquipmentConsistencyCheckProcessor,
                                                                      final FlatFileItemWriter<VirtualEquipment> unvalidatedVirtualEquipmentAfterConsistencyCheckItemWriter) {
        return new StepBuilder(VIRTUAL_EQUIPMENT_PHYSICAL_EQUIPMENT_CONSISTENCY_CHECK_STEP_NAME, jobRepository)
                .<VirtualEquipment, VirtualEquipment>chunk(10, transactionManager)
                .reader(virtualEquipmentNotLinkedToPhysicalEquipmentReader)
                .processor(virtualEquipmentPhysicalEquipmentConsistencyCheckProcessor)
                .taskExecutor(consistencyToPeTaskExecutor())
                .writer(unvalidatedVirtualEquipmentAfterConsistencyCheckItemWriter).build();
    }

    /**
     * Task executor.
     *
     * @return the simpleAsyncTaskExecutor
     */
    @Bean
    public TaskExecutor consistencyToPeTaskExecutor() {
        return new SimpleAsyncTaskExecutor("consistencyToPeTaskExecutor");
    }

    /**
     * Validated virtual equipment data RepositoryItemReader definition.
     *
     * @param virtualEquipmentRepository the repository to access virtual equipment data.
     * @param sessionDate                the identifying session of the batch
     * @param pageSize                   the repository reader page size.
     * @return the configured reader.
     */
    @Bean
    @StepScope
    public RepositoryItemReader<VirtualEquipment> virtualEquipmentNotLinkedToPhysicalEquipmentReader(final VirtualEquipmentRepository virtualEquipmentRepository,
                                                                                                     @Value("#{jobParameters['session.date']}") final Date sessionDate,
                                                                                                     @Value("${loading.batch.page-size}") final Integer pageSize) {
        final RepositoryItemReaderBuilder<VirtualEquipment> builder = new RepositoryItemReaderBuilder<>();
        return builder.name("virtualEquipmentNotLinkedToPhysicalEquipmentReader")
                .repository(virtualEquipmentRepository)
                .methodName("findVirtualEquipmentNotLinkedToPhysicalEquipment")
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
    public VirtualEquipmentPhysicalEquipmentConsistencyCheckProcessor virtualEquipmentPhysicalEquipmentConsistencyCheckProcessor(@Value("#{jobParameters['locale']}") final Locale locale,
                                                                                                                                 final MessageSource messageSource) {
        return new VirtualEquipmentPhysicalEquipmentConsistencyCheckProcessor(messageSource, locale);
    }

    /**
     * Consistency Check FlatFileItemWriter
     *
     * @param unvalidatedVirtualEquipmentItemWriter writer defined in FlagStepConfiguration.
     * @return the same writer defined in FlagStepConfiguration, except that it allows append.
     */
    @Bean
    @StepScope
    public FlatFileItemWriter<VirtualEquipment> unvalidatedVirtualEquipmentAfterConsistencyCheckItemWriter(final FlatFileItemWriter<VirtualEquipment> unvalidatedVirtualEquipmentItemWriter) {
        unvalidatedVirtualEquipmentItemWriter.setAppendAllowed(true);
        return unvalidatedVirtualEquipmentItemWriter;
    }

}
