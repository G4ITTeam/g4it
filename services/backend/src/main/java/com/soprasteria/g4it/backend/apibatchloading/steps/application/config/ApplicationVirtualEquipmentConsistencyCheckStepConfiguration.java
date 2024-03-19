/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apibatchloading.steps.application.config;

import com.soprasteria.g4it.backend.apibatchloading.steps.application.processor.ApplicationVirtualEquipmentConsistencyCheckProcessor;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Application;
import com.soprasteria.g4it.backend.apiinventory.repository.ApplicationRepository;
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
 * Application - Virtual Equipment Consistency check Step configuration.
 */
@Configuration
public class ApplicationVirtualEquipmentConsistencyCheckStepConfiguration {

    /**
     * The step name.
     */
    public static final String APPLICATION_VIRTUAL_EQUIPMENT_CONSISTENCY_CHECK_STEP_NAME = "applicationVirtualEquipmentConsistencyCheckStep";

    /**
     * Step definition.
     *
     * @param jobRepository                                         Spring Batch Job Repository.
     * @param transactionManager                                    the transaction manager (since Spring Batch v5).
     * @param applicationNotLinkedToVirtualEquipmentReader          the validated application data reader.
     * @param applicationVirtualEquipmentConsistencyCheckProcessor  the consistency check processor.
     * @param unvalidatedApplicationAfterConsistencyCheckItemWriter the ItemWriter to append in reject file.
     * @return the configured Step.
     */
    @Bean
    public Step applicationVirtualEquipmentConsistencyCheckStep(final JobRepository jobRepository,
                                                                final PlatformTransactionManager transactionManager,
                                                                final RepositoryItemReader<Application> applicationNotLinkedToVirtualEquipmentReader,
                                                                final ApplicationVirtualEquipmentConsistencyCheckProcessor applicationVirtualEquipmentConsistencyCheckProcessor,
                                                                final FlatFileItemWriter<Application> unvalidatedApplicationAfterConsistencyCheckItemWriter) {
        return new StepBuilder(APPLICATION_VIRTUAL_EQUIPMENT_CONSISTENCY_CHECK_STEP_NAME, jobRepository)
                .<Application, Application>chunk(10, transactionManager)
                .reader(applicationNotLinkedToVirtualEquipmentReader)
                .processor(applicationVirtualEquipmentConsistencyCheckProcessor)
                .writer(unvalidatedApplicationAfterConsistencyCheckItemWriter)
                .taskExecutor(consistencyToVmTaskExecutor())
                .build();
    }

    /**
     * Task executor.
     *
     * @return the simpleAsyncTaskExecutor
     */
    @Bean
    public TaskExecutor consistencyToVmTaskExecutor() {
        return new SimpleAsyncTaskExecutor("consistencyToVmTaskExecutor");
    }

    /**
     * Application not link to a virtual equipment RepositoryItemReader definition.
     *
     * @param applicationRepository the repository to access application data.
     * @param sessionDate           the identifying session of the batch
     * @param pageSize              the repository reader page size.
     * @return the configured reader.
     */
    @Bean
    @StepScope
    public RepositoryItemReader<Application> applicationNotLinkedToVirtualEquipmentReader(final ApplicationRepository applicationRepository,
                                                                                          @Value("#{jobParameters['session.date']}") final Date sessionDate,
                                                                                          @Value("${loading.batch.page-size}") final Integer pageSize) {
        final RepositoryItemReaderBuilder<Application> builder = new RepositoryItemReaderBuilder<>();
        return builder.name("applicationNotLinkedToVirtualEquipmentReader")
                .repository(applicationRepository)
                .methodName("findApplicationNotLinkedToVirtualEquipment")
                .arguments(sessionDate)
                .pageSize(pageSize)
                .sorts(Map.of("line_number", Sort.Direction.ASC))
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
    public ApplicationVirtualEquipmentConsistencyCheckProcessor applicationVirtualEquipmentConsistencyCheckProcessor(@Value("#{jobParameters['locale']}") final Locale locale,
                                                                                                                     final MessageSource messageSource) {
        return new ApplicationVirtualEquipmentConsistencyCheckProcessor(messageSource, locale);
    }

    /**
     * Consistency Check FlatFileItemWriter
     *
     * @param unvalidatedApplicationItemWriter writer defined in FlagStepConfiguration.
     * @return the same writer defined in FlagStepConfiguration, except that it allows append.
     */
    @Bean
    @StepScope
    public FlatFileItemWriter<Application> unvalidatedApplicationAfterConsistencyCheckItemWriter(final FlatFileItemWriter<Application> unvalidatedApplicationItemWriter) {
        unvalidatedApplicationItemWriter.setAppendAllowed(true);
        return unvalidatedApplicationItemWriter;
    }

}
