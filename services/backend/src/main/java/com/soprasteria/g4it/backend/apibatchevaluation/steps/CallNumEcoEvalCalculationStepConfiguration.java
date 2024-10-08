/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apibatchevaluation.steps;

import com.soprasteria.g4it.backend.apibatchevaluation.business.InventoryEvaluationService;
import com.soprasteria.g4it.backend.apibatchevaluation.tasklet.CallNumEcoEvalCalculationTasklet;
import com.soprasteria.g4it.backend.external.numecoeval.business.NumEcoEvalRemotingService;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CallNumEcoEvalCalculationStepConfiguration {

    /**
     * Step Configuration to call numEcoEval calculation api.
     *
     * @param jobRepository                    Spring Batch Job Repository.
     * @param transactionManager               the transaction manager (since Spring Batch v5).
     * @param callNumEcoEvalCalculationTasklet Tasklet to call num eco eval calculation api.
     * @return configured step.
     */
    @Bean
    public Step callNumEcoEvalCalculationStep(final JobRepository jobRepository,
                                              final PlatformTransactionManager transactionManager,
                                              final CallNumEcoEvalCalculationTasklet callNumEcoEvalCalculationTasklet) {
        return new StepBuilder("callNumEcoEvalCalculationStep", jobRepository)
                .tasklet(callNumEcoEvalCalculationTasklet, transactionManager)
                .build();
    }

    /**
     * Tasklet Config to remove Working Folder
     *
     * @param numEcoEvalRemotingService webclient to call numEcoEval.
     * @param batchName                 the batch name for numEcoEval.
     * @return configured tasklet.
     */
    @Bean
    @StepScope
    public CallNumEcoEvalCalculationTasklet callNumEcoEvalCalculationTasklet(final NumEcoEvalRemotingService numEcoEvalRemotingService,
                                                                             final InventoryEvaluationService inventoryEvaluationService,
                                                                             @Value("#{jobParameters['batch.name']}") final String batchName,
                                                                             @Value("#{jobParameters['inventory.criteria']}") final String inventoryCriteria) {
        List<String> criteriaList = null;
        if (inventoryCriteria != null && !inventoryCriteria.isEmpty()) {
            criteriaList = Arrays.stream(inventoryCriteria.split(","))
                    .map(String::trim)
                    .toList();
        }
        return new CallNumEcoEvalCalculationTasklet(numEcoEvalRemotingService, inventoryEvaluationService, batchName, criteriaList);
    }

}
