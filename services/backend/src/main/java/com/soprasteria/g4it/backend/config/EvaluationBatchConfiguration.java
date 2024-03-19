/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.config;

import com.soprasteria.g4it.backend.apibatchevaluation.listener.InventoryEvaluationJobListener;
import com.soprasteria.g4it.backend.apiinventory.repository.InventoryRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

/**
 * Evaluation Batch Configuration
 */
@Configuration
public class EvaluationBatchConfiguration {

    public static final String EVALUATE_INVENTORY_JOB = "evaluateInventoryJob";

    /**
     * Evaluation Job configuration
     *
     * @param jobRepository                         Spring Batch Job Repository.
     * @param extractDataFlow                       the extract data flow.
     * @param callNumEcoEvalInputDataExpositionStep call NumEcoEval input data exposition API Step.
     * @param callNumEcoEvalCalculationStep         call NumEcoEval calculation API Step.
     * @param cleanWorkingEvaluationFolderStep      clean working folder Step.
     * @param inventoryEvaluationJobListener        the job listener.
     * @return the configured Job.
     */
    @Bean
    public Job evaluateInventoryJob(final JobRepository jobRepository,
                                    final Flow extractDataFlow,
                                    final Step callNumEcoEvalInputDataExpositionStep,
                                    final Step cleanWorkingEvaluationFolderStep,
                                    final Step callNumEcoEvalCalculationStep,
                                    final InventoryEvaluationJobListener inventoryEvaluationJobListener) {
        return new JobBuilder(EVALUATE_INVENTORY_JOB, jobRepository)
                .listener(inventoryEvaluationJobListener)
                .start(extractDataFlow)
                .next(callNumEcoEvalInputDataExpositionStep)
                .next(cleanWorkingEvaluationFolderStep)
                .next(callNumEcoEvalCalculationStep)
                .build()
                .build();
    }

    /**
     * Job listener definition.
     *
     * @param inventoryRepository the repository to access inventory data.
     * @return the configured listener.
     */
    @Bean
    public InventoryEvaluationJobListener inventoryEvaluationJobListener(final InventoryRepository inventoryRepository) {
        return new InventoryEvaluationJobListener(inventoryRepository);
    }

    /**
     * Parallel Data extraction Flow.
     *
     * @param dataCenterExtractionFlow        the flow to extract DataCenter data.
     * @param physicalEquipmentExtractionFlow the flow to extract PhysicalEquipment data.
     * @param virtualEquipmentExtractionFlow  the flow to extract VirtualEquipment data.
     * @param applicationExtractionFlow       the flow to extract Application data
     * @return the configured Flow.
     */
    @Bean
    public Flow extractDataFlow(final Flow dataCenterExtractionFlow,
                                final Flow physicalEquipmentExtractionFlow,
                                final Flow virtualEquipmentExtractionFlow,
                                final Flow applicationExtractionFlow) {
        return new FlowBuilder<SimpleFlow>("writeValidOutputFilesFlow")
                .split(extractDataTaskExecutor())
                .add(dataCenterExtractionFlow, physicalEquipmentExtractionFlow, virtualEquipmentExtractionFlow, applicationExtractionFlow)
                .build();
    }

    /**
     * Parallel Task Executor Configuration.
     *
     * @return the configured taskExecutor.
     */
    @Bean
    public TaskExecutor extractDataTaskExecutor() {
        return new SimpleAsyncTaskExecutor("extractDataTaskExecutor");
    }

    /**
     * Async Job Launcher.
     *
     * @param jobRepository the Spring JobRepository.
     * @return the configured async job launcher.
     * @throws Exception when errors occurs.
     */
    @Bean
    public JobLauncher asyncEvaluationJobLauncher(final JobRepository jobRepository) throws Exception {
        final TaskExecutorJobLauncher jobLauncher = new TaskExecutorJobLauncher();
        jobLauncher.setJobRepository(jobRepository);
        jobLauncher.setTaskExecutor(new SimpleAsyncTaskExecutor());
        jobLauncher.afterPropertiesSet();
        return jobLauncher;
    }

}
