/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apibatchexport.config.step;

import com.soprasteria.g4it.backend.apibatchexport.business.InventoryExportService;
import com.soprasteria.g4it.backend.apibatchexport.tasklet.LoadSipReferentialTasklet;
import com.soprasteria.g4it.backend.apiindicator.repository.RefSustainableIndividualPackageRepository;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Loading referential step configuration.
 */
@Configuration
public class LoadSipReferentialStepConfiguration {

    public static final String SIP_CONTEXT_KEY = "sip.list";

    /**
     * Step definition.
     *
     * @param jobRepository             Spring Batch Job Repository.
     * @param transactionManager        the transaction manager (since Spring Batch v5).
     * @param loadSipReferentialTasklet tasklet to get referential from database.
     * @return the configured Step.
     */
    @Bean
    public Step loadSipReferentialStep(final JobRepository jobRepository,
                                       final PlatformTransactionManager transactionManager,
                                       final LoadSipReferentialTasklet loadSipReferentialTasklet) {
        return new StepBuilder("loadSipReferentialStep", jobRepository)
                .tasklet(loadSipReferentialTasklet, transactionManager)
                .build();
    }

    /**
     * Tasklet definition.
     *
     * @param refSustainableIndividualPackageRepository the repository to access referential.
     * @return the configured tasklet.
     */
    @Bean
    @StepScope
    public LoadSipReferentialTasklet loadSipReferentialTasklet(final RefSustainableIndividualPackageRepository refSustainableIndividualPackageRepository,
                                                               @Value("#{jobParameters['batch.name']}") final String batchName,
                                                               final InventoryExportService inventoryExportService) {
        return new LoadSipReferentialTasklet(batchName, refSustainableIndividualPackageRepository, inventoryExportService);
    }

}
