/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apibatchloading.steps.physicalequipment;

import com.soprasteria.g4it.backend.apibatchloading.steps.common.tasklet.CheckFolderTasklet;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Physical equipment Flow Configuration.
 */
@Configuration
public class PhysicalEquipmentFlowConfiguration {

    /**
     * Physical Equipment Flow Configuration.
     *
     * @param validatePhysicalEquipmentHeaderStep Step to validate physical equipment input file headers.
     * @param flagPhysicalEquipmentDataStep       Step to validate physical equipment data.
     * @return the configured Flow
     */
    @Bean
    public Flow physicalEquipmentFlow(
            final Step checkPhysicalEquipmentFolderStep,
            final Step validatePhysicalEquipmentHeaderStep,
            final Step flagPhysicalEquipmentDataStep) {
        return new FlowBuilder<Flow>("physicalEquipmentFlow")
                .start(checkPhysicalEquipmentFolderStep)
                .on(CheckFolderTasklet.PROCESS_EXIT_STATUS).to(validatePhysicalEquipmentHeaderStep)
                .on(BatchStatus.COMPLETED.name()).to(flagPhysicalEquipmentDataStep)
                .from(checkPhysicalEquipmentFolderStep)
                .on("*").end()
                .from(validatePhysicalEquipmentHeaderStep)
                .on("*").end()
                .build();
    }

    /**
     * Write Valid PhysicalEquipment Flow Configuration.
     *
     * @param writeValidatedPhysicalEquipmentDataStep Step to write valid PhysicalEquipment Data.
     * @return the configured Flow.
     */
    @Bean
    public Flow writeValidPhysicalEquipmentFlow(final Step writeValidatedPhysicalEquipmentDataStep) {
        return new FlowBuilder<SimpleFlow>("writeValidPhysicalEquipmentFlow")
                .start(writeValidatedPhysicalEquipmentDataStep)
                .build();
    }

    /**
     * Delete after consistency check Flow Configuration.
     *
     * @param physicalEquipmentDataCenterConsistencyCheckStep  Step to write physical equipment not linked to physical equipment.
     * @param deletePhysicalEquipmentAfterConsistencyCheckStep Step to delete from database.
     * @return the configured flow.
     */
    @Bean
    public Flow physicalEquipmentConsistencyCheckFlow(final Step physicalEquipmentDataCenterConsistencyCheckStep,
                                                      final Step deletePhysicalEquipmentAfterConsistencyCheckStep) {
        return new FlowBuilder<SimpleFlow>("physicalEquipmentConsistencyCheckFlow")
                .start(physicalEquipmentDataCenterConsistencyCheckStep)
                .next(deletePhysicalEquipmentAfterConsistencyCheckStep)
                .build();
    }
}
