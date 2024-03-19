/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apibatchloading.steps.application;

import com.soprasteria.g4it.backend.apibatchloading.steps.common.tasklet.CheckFolderTasklet;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Application Flow Configuration.
 */
@Configuration
public class ApplicationFlowConfiguration {

    /**
     * Application Flow Configuration.
     *
     * @param validateApplicationHeaderStep Step to validate application input file headers.
     * @param flagApplicationDataStep       Step to validate application data.
     * @return the configured Flow
     */
    @Bean
    public Flow applicationFlow(
            final Step checkApplicationFolderStep,
            final Step validateApplicationHeaderStep,
            final Step flagApplicationDataStep) {
        return new FlowBuilder<Flow>("applicationFlow")
                .start(checkApplicationFolderStep)
                .on(CheckFolderTasklet.PROCESS_EXIT_STATUS).to(validateApplicationHeaderStep)
                .on(BatchStatus.COMPLETED.name()).to(flagApplicationDataStep)
                .from(checkApplicationFolderStep)
                .on("*").end()
                .from(validateApplicationHeaderStep)
                .on("*").end()
                .build();
    }

    /**
     * Write Valid Application Flow Configuration.
     *
     * @param writeValidatedApplicationDataStep Step to write valid Application Data.
     * @return the configured Flow.
     */
    @Bean
    public Flow writeValidApplicationFlow(final Step writeValidatedApplicationDataStep) {
        return new FlowBuilder<SimpleFlow>("writeValidApplicationFlow")
                .start(writeValidatedApplicationDataStep)
                .build();
    }

    /**
     * Delete after consistency check Flow Configuration.
     *
     * @param applicationVirtualEquipmentConsistencyCheckStep Step to write application not linked to virtual equipment.
     * @param deleteApplicationAfterConsistencyCheckStep      Step to delete from database.
     * @return the configured flow.
     */
    @Bean
    public Flow applicationConsistencyCheckFlow(final Step applicationVirtualEquipmentConsistencyCheckStep,
                                                final Step deleteApplicationAfterConsistencyCheckStep) {
        return new FlowBuilder<SimpleFlow>("applicationConsistencyCheckFlow")
                .start(applicationVirtualEquipmentConsistencyCheckStep)
                .next(deleteApplicationAfterConsistencyCheckStep)
                .build();
    }
}
