/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apibatchloading.steps.common.tasklet;

import com.soprasteria.g4it.backend.apibatchloading.steps.common.config.LoadReferentialStepConfiguration;
import com.soprasteria.g4it.backend.external.numecoeval.business.NumEcoEvalReferentialRemotingService;
import lombok.AllArgsConstructor;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;

/**
 * Load referential tasklet.
 */
@AllArgsConstructor
public class LoadReferentialTasklet implements Tasklet {

    /**
     * Service to get numecoeval referential data.
     */
    private NumEcoEvalReferentialRemotingService numEcoEvalReferentialRemotingService;

    /**
     * {@inheritDoc}
     */
    @Override
    public RepeatStatus execute(final StepContribution contribution, final ChunkContext chunkContext) throws Exception {
        final ExecutionContext executionContext = contribution.getStepExecution().getJobExecution().getExecutionContext();
        executionContext.put(LoadReferentialStepConfiguration.COUNTRIES_CONTEXT_KEY, numEcoEvalReferentialRemotingService.getCountryList());
        executionContext.put(LoadReferentialStepConfiguration.EQUIPMENT_TYPES_CONTEXT_KEY, numEcoEvalReferentialRemotingService.getEquipmentTypeList());
        return RepeatStatus.FINISHED;
    }
}
