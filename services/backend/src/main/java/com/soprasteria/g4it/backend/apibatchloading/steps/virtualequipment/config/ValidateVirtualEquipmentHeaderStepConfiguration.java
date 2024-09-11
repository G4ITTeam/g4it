/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apibatchloading.steps.virtualequipment.config;

import com.soprasteria.g4it.backend.apibatchloading.steps.common.tasklet.ValidateHeaderTasklet;
import com.soprasteria.g4it.backend.common.filesystem.business.FileStorage;
import com.soprasteria.g4it.backend.common.filesystem.model.FileMapperInfo;
import com.soprasteria.g4it.backend.common.filesystem.model.FileType;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * VirtualEquipment headers validation Step configuration.
 */
@Configuration
public class ValidateVirtualEquipmentHeaderStepConfiguration {

    /**
     * The step name.
     */
    public static final String VALIDATE_VIRTUAL_EQUIPMENT_HEADER_STEP_NAME = "validateVirtualEquipmentHeaderStep";

    /**
     * Step to validate virtual equipment file headers.
     *
     * @param jobRepository                         Spring Batch Job Repository.
     * @param transactionManager                    the transaction manager (since Spring Batch v5).
     * @param validateVirtualEquipmentHeaderTasklet the tasklet to validate virtual equipment file headers.
     * @return the configured step.
     */
    @Bean
    public Step validateVirtualEquipmentHeaderStep(final JobRepository jobRepository,
                                                   final PlatformTransactionManager transactionManager,
                                                   final ValidateHeaderTasklet validateVirtualEquipmentHeaderTasklet) {
        return new StepBuilder(VALIDATE_VIRTUAL_EQUIPMENT_HEADER_STEP_NAME, jobRepository)
                .tasklet(validateVirtualEquipmentHeaderTasklet, transactionManager)
                .build();
    }

    /**
     * Validation header tasklet configuration.
     *
     * @param sessionPath        the session date formatted.
     * @param fileInfo           to get file information.
     * @param loadingFileStorage to get input files.
     * @return the configured tasklet.
     */
    @Bean
    @StepScope
    public ValidateHeaderTasklet validateVirtualEquipmentHeaderTasklet(
            @Value("#{jobExecutionContext['session.path']}") final String sessionPath, final FileMapperInfo fileInfo, final FileStorage loadingFileStorage) {
        return new ValidateHeaderTasklet(loadingFileStorage, FileType.EQUIPEMENT_VIRTUEL, sessionPath, fileInfo.getMapping(FileType.EQUIPEMENT_VIRTUEL));
    }
}
