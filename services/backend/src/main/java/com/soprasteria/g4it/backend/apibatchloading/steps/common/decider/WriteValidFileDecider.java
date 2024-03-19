/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apibatchloading.steps.common.decider;

import lombok.AllArgsConstructor;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;

/**
 * Write valid file decider.
 */
@AllArgsConstructor
public class WriteValidFileDecider implements JobExecutionDecider {

    /**
     * Flag indicating to write valid file or not.
     */
    private boolean writeValidFile;

    /**
     * {@inheritDoc}
     */
    @Override
    public FlowExecutionStatus decide(final JobExecution jobExecution, final StepExecution stepExecution) {
        if (writeValidFile) {
            return new FlowExecutionStatus("EXECUTE");
        } else {
            return new FlowExecutionStatus("IGNORE");
        }
    }
}
