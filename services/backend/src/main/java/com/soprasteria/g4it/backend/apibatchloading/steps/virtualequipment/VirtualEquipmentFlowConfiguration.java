/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apibatchloading.steps.virtualequipment;

import com.soprasteria.g4it.backend.apibatchloading.steps.common.tasklet.CheckFolderTasklet;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Virtual equipment Flow Configuration.
 */
@Configuration
public class VirtualEquipmentFlowConfiguration {

    /**
     * Virtual Equipment Flow Configuration.
     *
     * @param validateVirtualEquipmentHeaderStep Step to validate virtual equipment input file headers.
     * @param flagVirtualEquipmentDataStep       Step to validate virtual equipment data.
     * @return the configured Flow
     */
    @Bean
    public Flow virtualEquipmentFlow(
            final Step checkVirtualEquipmentFolderStep,
            final Step validateVirtualEquipmentHeaderStep,
            final Step flagVirtualEquipmentDataStep) {
        return new FlowBuilder<Flow>("virtualEquipmentFlow")
                .start(checkVirtualEquipmentFolderStep)
                .on(CheckFolderTasklet.PROCESS_EXIT_STATUS).to(validateVirtualEquipmentHeaderStep)
                .on(BatchStatus.COMPLETED.name()).to(flagVirtualEquipmentDataStep)
                .from(checkVirtualEquipmentFolderStep)
                .on("*").end()
                .from(validateVirtualEquipmentHeaderStep)
                .on("*").end()
                .build();
    }

    /**
     * Write Valid VirtualEquipment Flow Configuration.
     *
     * @param writeValidatedVirtualEquipmentDataStep Step to write valid VirtualEquipment Data.
     * @return the configured Flow.
     */
    @Bean
    public Flow writeValidVirtualEquipmentFlow(final Step writeValidatedVirtualEquipmentDataStep) {
        return new FlowBuilder<SimpleFlow>("writeValidVirtualEquipmentFlow")
                .start(writeValidatedVirtualEquipmentDataStep)
                .build();
    }

    /**
     * Delete after consistency check Flow Configuration.
     *
     * @param virtualEquipmentPhysicalEquipmentConsistencyCheckStep Step to write virtual equipment not linked to physical equipment.
     * @param deleteVirtualEquipmentAfterConsistencyCheckStep       Step to delete from database.
     * @return the configured flow.
     */
    @Bean
    public Flow virtualEquipmentConsistencyCheckFlow(final Step virtualEquipmentPhysicalEquipmentConsistencyCheckStep,
                                                     final Step deleteVirtualEquipmentAfterConsistencyCheckStep) {
        return new FlowBuilder<SimpleFlow>("virtualEquipmentConsistencyCheckFlow")
                .start(virtualEquipmentPhysicalEquipmentConsistencyCheckStep)
                .next(deleteVirtualEquipmentAfterConsistencyCheckStep)
                .build();
    }
}
