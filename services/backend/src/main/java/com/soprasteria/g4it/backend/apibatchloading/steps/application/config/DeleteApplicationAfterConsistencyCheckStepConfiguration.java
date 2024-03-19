/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apibatchloading.steps.application.config;

import com.soprasteria.g4it.backend.apibatchloading.steps.application.tasklet.DeleteAfterConsistencyCheckTasklet;
import com.soprasteria.g4it.backend.apiinventory.repository.ApplicationRepository;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Date;

/**
 * Delete all not linked virtual equipments Tasklet Configuration.
 */
@Configuration
public class DeleteApplicationAfterConsistencyCheckStepConfiguration {

    /**
     * Step definition.
     *
     * @param jobRepository                                 Spring Batch Job Repository.
     * @param transactionManager                            the transaction manager (since Spring Batch v5).
     * @param deleteApplicationAfterConsistencyCheckTasklet Tasklet to delete data after consistency checks.
     * @return the configured Step.
     */
    @Bean
    public Step deleteApplicationAfterConsistencyCheckStep(final JobRepository jobRepository,
                                                           final PlatformTransactionManager transactionManager,
                                                           final DeleteAfterConsistencyCheckTasklet deleteApplicationAfterConsistencyCheckTasklet) {
        return new StepBuilder("deleteApplicationAfterConsistencyCheckStep", jobRepository)
                .tasklet(deleteApplicationAfterConsistencyCheckTasklet, transactionManager)
                .build();
    }

    /**
     * Tasklet definition.
     *
     * @param applicationRepository the repository to access data.
     * @param sessionDate           the session date.
     * @return the configured tasklet.
     */
    @Bean
    @StepScope
    public DeleteAfterConsistencyCheckTasklet deleteApplicationAfterConsistencyCheckTasklet(final ApplicationRepository applicationRepository, @Value("#{jobParameters['session.date']}") final Date sessionDate) {
        return new DeleteAfterConsistencyCheckTasklet(applicationRepository, sessionDate);
    }
}
