/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apibatchevaluation.tasklet;

import com.soprasteria.g4it.backend.apibatchevaluation.business.InventoryEvaluationService;
import com.soprasteria.g4it.backend.common.utils.EvaluationBatchStatus;
import com.soprasteria.g4it.backend.external.numecoeval.business.NumEcoEvalRemotingService;
import lombok.AllArgsConstructor;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

/**
 * Tasklet to call NumEcoEval input data exposition.
 */
@AllArgsConstructor
public class CallNumEcoEvalCalculationTasklet implements Tasklet {

    /**
     * Service Remoting.
     */
    private NumEcoEvalRemotingService numEcoEvalRemotingService;

    /**
     * Inventory evaluation service
     */
    private InventoryEvaluationService inventoryEvaluationService;

    /**
     * The batch name for numEcoEval.
     */
    private String batchName;

    /**
     * {@inheritDoc}
     */
    @Override
    public RepeatStatus execute(final StepContribution stepContribution, final ChunkContext chunkContext) throws Exception {
        inventoryEvaluationService.updateBatchStatus(batchName, EvaluationBatchStatus.CALCUL_SUBMISSION_TO_NUMECOVAL);
        numEcoEvalRemotingService.callCalculation(batchName);
        return RepeatStatus.FINISHED;
    }

}
