/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apibatchloading.steps.upload;

import com.soprasteria.g4it.backend.apibatchloading.steps.upload.tasklet.UploadResultTasklet;
import com.soprasteria.g4it.backend.common.filesystem.business.FileStorage;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.nio.file.Path;

/**
 * Step configuration to upload result to filesystem
 */
@Configuration
public class UploadResultStepConfiguration {

    /**
     * Step name.
     */
    public static final String UPLOAD_STEP_NAME = "uploadResultStep";

    /**
     * Step Configuration to upload files at the end
     *
     * @param jobRepository         Spring Batch Job Repository.
     * @param transactionManager    the transaction manager (since Spring Batch v5).
     * @param uploadOkResultTasklet Tasklet for ok results
     * @return configured step.
     */
    @Bean
    public Step uploadResultStep(final JobRepository jobRepository,
                                 final PlatformTransactionManager transactionManager,
                                 final UploadResultTasklet uploadOkResultTasklet) {
        return new StepBuilder(UPLOAD_STEP_NAME, jobRepository)
                .tasklet(uploadOkResultTasklet, transactionManager)
                .build();
    }

    /**
     * Tasklet Config to upload OK file results
     *
     * @param sessionPath        the formatted session path.
     * @param localWorkingFolder generated local working folder
     * @param loadingFileStorage file storage to handle files operations
     * @param zipResults         boolean to toggle archive creation
     * @return configured tasklet.
     */
    @Bean
    @StepScope
    public UploadResultTasklet uploadOkResultTasklet(
            @Value("#{jobExecutionContext['session.path']}") final String sessionPath,
            @Value("#{jobParameters['local.working.folder']}") final String localWorkingFolder,
            @Value("#{jobParameters['zip.results']}") final String zipResults,
            final FileStorage loadingFileStorage) {
        return new UploadResultTasklet(loadingFileStorage, Path.of(localWorkingFolder), sessionPath, Boolean.parseBoolean(zipResults));
    }

}
