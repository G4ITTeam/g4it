/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apibatchexport.tasklet;

import com.soprasteria.g4it.backend.apibatchexport.config.step.LoadSipReferentialStepConfiguration;
import com.soprasteria.g4it.backend.apiindicator.modeldb.RefSustainableIndividualPackage;
import com.soprasteria.g4it.backend.apiindicator.repository.RefSustainableIndividualPackageRepository;
import lombok.AllArgsConstructor;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;

import java.util.stream.Collectors;

/**
 * Load referential tasklet.
 */
@AllArgsConstructor
public class LoadSipReferentialTasklet implements Tasklet {

    /**
     * Repository to get referential data.
     */
    private RefSustainableIndividualPackageRepository refSustainableIndividualPackageRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public RepeatStatus execute(final StepContribution contribution, final ChunkContext chunkContext) throws Exception {
        final ExecutionContext executionContext = contribution.getStepExecution().getJobExecution().getExecutionContext();

        executionContext.put(LoadSipReferentialStepConfiguration.SIP_CONTEXT_KEY, refSustainableIndividualPackageRepository
                .findAll()
                .stream()
                .collect(Collectors
                        .toMap(RefSustainableIndividualPackage::getCriteria, RefSustainableIndividualPackage::getIndividualSustainablePackage)));
        return RepeatStatus.FINISHED;
    }
}
