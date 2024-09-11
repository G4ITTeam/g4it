/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apibatchloading.steps.virtualequipment.config;

import com.soprasteria.g4it.backend.apibatchloading.steps.common.tasklet.CheckFolderTasklet;
import com.soprasteria.g4it.backend.common.filesystem.business.FileStorage;
import com.soprasteria.g4it.backend.common.filesystem.model.FileType;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class CheckVirtualEquipmentFolderStepConfiguration {

    /**
     * Step definition.
     *
     * @param jobRepository                      Spring Batch Job Repository.
     * @param transactionManager                 the transaction manager (since Spring Batch v5).
     * @param checkVirtualEquipmentFolderTasklet Tasklet to check files in input folder.
     * @return the configured Step.
     */
    @Bean
    public Step checkVirtualEquipmentFolderStep(final JobRepository jobRepository,
                                                final PlatformTransactionManager transactionManager,
                                                final CheckFolderTasklet checkVirtualEquipmentFolderTasklet) {
        return new StepBuilder("checkVirtualEquipmentFolderTasklet", jobRepository)
                .tasklet(checkVirtualEquipmentFolderTasklet, transactionManager)
                .build();
    }

    /**
     * Tasklet definition.
     *
     * @param loadingFileStorage file storage
     * @param sessionPath        folder path for this session
     * @return the configured tasklet.
     */
    @Bean
    @StepScope
    public CheckFolderTasklet checkVirtualEquipmentFolderTasklet(final FileStorage loadingFileStorage, @Value("#{jobExecutionContext['session.path']}") final String sessionPath) {
        return new CheckFolderTasklet(loadingFileStorage, sessionPath, FileType.EQUIPEMENT_VIRTUEL);
    }
}
