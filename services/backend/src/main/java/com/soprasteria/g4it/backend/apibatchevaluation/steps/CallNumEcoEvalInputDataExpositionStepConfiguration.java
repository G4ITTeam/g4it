/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apibatchevaluation.steps;

import com.soprasteria.g4it.backend.apibatchevaluation.business.InventoryEvaluationService;
import com.soprasteria.g4it.backend.apibatchevaluation.tasklet.CallNumEcoEvalInputDataExpositionTasklet;
import com.soprasteria.g4it.backend.external.numecoeval.business.NumEcoEvalRemotingService;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import java.nio.file.Path;
import java.util.Date;

@Configuration
public class CallNumEcoEvalInputDataExpositionStepConfiguration {

    /**
     * Step Configuration to call numEcoEval
     *
     * @param jobRepository                            Spring Batch Job Repository.
     * @param transactionManager                       the transaction manager (since Spring Batch v5).
     * @param callNumEcoEvalInputDataExpositionTasklet Tasklet to call num eco eval input data exposition api.
     * @return configured step.
     */
    @Bean
    public Step callNumEcoEvalInputDataExpositionStep(final JobRepository jobRepository,
                                                      final PlatformTransactionManager transactionManager,
                                                      final CallNumEcoEvalInputDataExpositionTasklet callNumEcoEvalInputDataExpositionTasklet) {
        return new StepBuilder("callNumEcoEvalInputDataExpositionStep", jobRepository)
                .tasklet(callNumEcoEvalInputDataExpositionTasklet, transactionManager)
                .build();
    }

    /**
     * Tasklet Config to remove Working Folder
     *
     * @param numEcoEvalRemotingService webclient to call numEcoEval.
     * @param batchName                 the batch name for numEcoEval.
     * @param processingDate            the processing date.
     * @param organization              the known organization.
     * @param localWorkingFolder        the local working path.
     * @return configured tasklet.
     */
    @Bean
    @StepScope
    public CallNumEcoEvalInputDataExpositionTasklet callNumEcoEvalInputDataExpositionTasklet(final NumEcoEvalRemotingService numEcoEvalRemotingService,
                                                                                             final InventoryEvaluationService inventoryEvaluationService,
                                                                                             @Value("#{jobParameters['batch.name']}") final String batchName,
                                                                                             @Value("#{jobParameters['processing.date']}") final Date processingDate,
                                                                                             @Value("#{jobParameters['organization']}") final String organization,
                                                                                             @Value("#{jobParameters['local.working.folder']}") final String localWorkingFolder) {
        return new CallNumEcoEvalInputDataExpositionTasklet(numEcoEvalRemotingService,
                inventoryEvaluationService,
                batchName, processingDate, organization,
                new FileSystemResource(Path.of(localWorkingFolder, "datacenter.csv")),
                new FileSystemResource(Path.of(localWorkingFolder, "physical_equipment.csv")),
                new FileSystemResource(Path.of(localWorkingFolder, "virtual_equipment.csv")),
                new FileSystemResource(Path.of(localWorkingFolder, "application.csv")));
    }

}
