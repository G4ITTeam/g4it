/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apibatchloading.steps.common.config;

import com.soprasteria.g4it.backend.apibatchloading.steps.common.tasklet.LoadReferentialTasklet;
import com.soprasteria.g4it.backend.external.numecoeval.business.NumEcoEvalReferentialRemotingService;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Loading referential step configuration.
 */
@Configuration
public class LoadReferentialStepConfiguration {

    public static final String COUNTRIES_CONTEXT_KEY = "countries.list";
    public static final String EQUIPMENT_TYPES_CONTEXT_KEY = "equipmentType.list";

    /**
     * Step definition.
     *
     * @param jobRepository          Spring Batch Job Repository.
     * @param transactionManager     the transaction manager (since Spring Batch v5).
     * @param loadReferentialTasklet tasklet to get referential from numEcoEval.
     * @return the configured Step.
     */
    @Bean
    public Step loadReferentialStep(final JobRepository jobRepository,
                                    final PlatformTransactionManager transactionManager,
                                    final LoadReferentialTasklet loadReferentialTasklet) {
        return new StepBuilder("loadReferentialStep", jobRepository)
                .tasklet(loadReferentialTasklet, transactionManager)
                .build();
    }

    /**
     * Tasklet definition.
     *
     * @param numEcoEvalReferentialRemotingService service to get referential from numecoeval.
     * @return the configured tasklet.
     */
    @Bean
    @StepScope
    public LoadReferentialTasklet loadReferentialTasklet(final NumEcoEvalReferentialRemotingService numEcoEvalReferentialRemotingService) {
        return new LoadReferentialTasklet(numEcoEvalReferentialRemotingService);
    }

}
