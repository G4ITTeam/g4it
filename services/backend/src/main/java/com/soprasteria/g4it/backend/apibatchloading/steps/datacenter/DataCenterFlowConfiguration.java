/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apibatchloading.steps.datacenter;

import com.soprasteria.g4it.backend.apibatchloading.steps.common.tasklet.CheckFolderTasklet;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * DataCenter Flow Configuration.
 */
@Configuration
@ComponentScan(basePackages = {"com.soprasteria.g4it.backend.api.batch.loading.batch.steps.datacenter.config"})
public class DataCenterFlowConfiguration {

    /**
     * DataCenter Flow Configuration.
     *
     * @param validateDataCenterHeaderStep Step to validate DataCenter input file headers.
     * @param flagDataCenterDataStep       Step to validate DataCenter data.
     * @return the configured Flow.
     */
    @Bean
    public Flow dataCenterFlow(final Step checkDataCenterFolderStep,
                               final Step validateDataCenterHeaderStep,
                               final Step flagDataCenterDataStep) {
        return new FlowBuilder<Flow>("dataCenterFlow")
                .start(checkDataCenterFolderStep)
                .on(CheckFolderTasklet.PROCESS_EXIT_STATUS).to(validateDataCenterHeaderStep)
                .on(BatchStatus.COMPLETED.name())
                .to(flagDataCenterDataStep)
                .from(checkDataCenterFolderStep)
                .on("*").end()
                .from(validateDataCenterHeaderStep)
                .on("*").end()
                .build();
    }

    /**
     * Write Valid DataCenter Flow Configuration.
     *
     * @param writeValidatedDataCenterDataStep Step to write valid DataCenter Data.
     * @return the configured Flow.
     */
    @Bean
    public Flow writeValidDataCenterFlow(final Step writeValidatedDataCenterDataStep) {
        return new FlowBuilder<SimpleFlow>("writeValidDataCenterFlow")
                .start(writeValidatedDataCenterDataStep)
                .build();
    }

}
