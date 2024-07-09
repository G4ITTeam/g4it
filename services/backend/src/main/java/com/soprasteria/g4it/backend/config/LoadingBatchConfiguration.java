/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.config;

import com.soprasteria.g4it.backend.apibatchloading.listener.InventoryJobExecutionListener;
import com.soprasteria.g4it.backend.apibatchloading.steps.common.decider.WriteValidFileDecider;
import com.soprasteria.g4it.backend.apiinventory.repository.ApplicationRepository;
import com.soprasteria.g4it.backend.apiinventory.repository.InventoryRepository;
import com.soprasteria.g4it.backend.apiinventory.repository.PhysicalEquipmentRepository;
import com.soprasteria.g4it.backend.common.filesystem.model.FileStorage;
import com.soprasteria.g4it.backend.common.filesystem.model.FileSystem;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

/**
 * Integration processing configuration class.
 */
@Configuration
public class LoadingBatchConfiguration {

    public static final String LOAD_INVENTORY_JOB = "loadInventoryJob";

    /**
     * LoadInventory Job definition.
     *
     * @param jobRepository                         the Spring Job Repository to access spring metadata.
     * @param jobCompletionNotificationListener     the Job Processing listener.
     * @param loadReferentialStep                   the step to load referential data.
     * @param dataCenterFlow                        the flow to integrate or reject datacenter data.
     * @param physicalEquipmentFlow                 the flow to integrate or reject physical equipment data.
     * @param virtualEquipmentFlow                  the flow to integrate or reject virtual equipment.
     * @param applicationFlow                       the flow to integrate or reject application.
     * @param physicalEquipmentConsistencyCheckFlow the flow to process consistency control.
     * @param virtualEquipmentConsistencyCheckFlow  the flow to process consistency control.
     * @param applicationConsistencyCheckFlow       the flow to process consistency control.
     * @param writeValidOutputFilesFlow             the flow to write valid output file.
     * @param uploadResultStep                      the step to upload output files.
     * @param cleanWorkingFolderStep                the step to clean working folder.
     * @param writeValidFileDecider                 the decider to write valid files.
     * @return the configured job.
     */
    @Bean
    public Job loadInventoryJob(final JobRepository jobRepository,
                                final JobExecutionListener jobCompletionNotificationListener,
                                final Step loadReferentialStep,
                                final Flow dataCenterFlow,
                                final Flow physicalEquipmentFlow,
                                final Flow virtualEquipmentFlow,
                                final Flow applicationFlow,
                                final Flow physicalEquipmentConsistencyCheckFlow,
                                final Flow virtualEquipmentConsistencyCheckFlow,
                                final Flow applicationConsistencyCheckFlow,
                                final Flow writeValidOutputFilesFlow,
                                final Step uploadResultStep,
                                final Step cleanWorkingFolderStep,
                                final WriteValidFileDecider writeValidFileDecider) {
        return new JobBuilder(LOAD_INVENTORY_JOB, jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(jobCompletionNotificationListener)
                .start(loadReferentialStep)
                .on("*").to(dataCenterFlow)
                .next(physicalEquipmentFlow)
                .next(virtualEquipmentFlow)
                .next(applicationFlow)
                .next(physicalEquipmentConsistencyCheckFlow)
                .next(virtualEquipmentConsistencyCheckFlow)
                .next(applicationConsistencyCheckFlow)
                .next(writeValidFileDecider)
                .on("EXECUTE").to(writeValidOutputFilesFlow)
                .from(writeValidFileDecider).on("IGNORE").to(uploadResultStep)
                .from(writeValidOutputFilesFlow).on("*").to(uploadResultStep)
                .from(uploadResultStep).on("*").to(cleanWorkingFolderStep)
                .build()
                .build();
    }

    /**
     * Parallel Valid Data Write Flow.
     *
     * @param writeValidDataCenterFlow        the flow to write valid DataCenter data.
     * @param writeValidPhysicalEquipmentFlow the flow to write valid PhysicalEquipment data.
     * @param writeValidVirtualEquipmentFlow  the flow to write valid VirtualEquipment data.
     * @param writeValidApplicationFlow       the flow to write valid Application data
     * @return the configured Flow.
     */
    @Bean
    public Flow writeValidOutputFilesFlow(final Flow writeValidDataCenterFlow,
                                          final Flow writeValidPhysicalEquipmentFlow,
                                          final Flow writeValidVirtualEquipmentFlow,
                                          final Flow writeValidApplicationFlow) {
        return new FlowBuilder<SimpleFlow>("writeValidOutputFilesFlow")
                .split(writeValidOutputFilesTaskExecutor())
                .add(writeValidDataCenterFlow, writeValidPhysicalEquipmentFlow, writeValidVirtualEquipmentFlow, writeValidApplicationFlow)
                .build();
    }

    /**
     * Parallel Task Executor Configuration.
     *
     * @return the configured taskExecutor.
     */
    @Bean
    public TaskExecutor writeValidOutputFilesTaskExecutor() {
        return new SimpleAsyncTaskExecutor("writeValidOutputFilesTaskExecutor");
    }

    /**
     * Processing listener definition.
     *
     * @param inventoryRepository         the JPA repository to access inventories data.
     * @param physicalEquipmentRepository the JPA repository to access physicals equipments data.
     * @param applicationRepository       the JPA repository to access applications data.
     * @return the configured processing listener.
     */
    @Bean
    public JobExecutionListener jobCompletionNotificationListener(final InventoryRepository inventoryRepository,
                                                                  final PhysicalEquipmentRepository physicalEquipmentRepository,
                                                                  final ApplicationRepository applicationRepository) {
        return new InventoryJobExecutionListener(inventoryRepository, physicalEquipmentRepository, applicationRepository);
    }

    /**
     * Inject configured FileStorage in all jobs
     *
     * @param subscriber     the client subscriber.
     * @param organizationId the subscriber's organization.
     * @param fileSystem     abstraction over local or distant filesystem
     * @return a filestorage to interact with files
     */
    @Bean
    @JobScope
    public FileStorage loadingFileStorage(@Value("#{jobParameters['subscriber']}") final String subscriber,
                                          @Value("#{jobParameters['organization.id']}") final Long organizationId,
                                          final FileSystem fileSystem) {
        return fileSystem.mount(subscriber, organizationId.toString());
    }

    /**
     * Launcher to process the batch asynchronously.
     *
     * @param jobRepository the spring job repository to access spring metadata.
     * @return the configured job launcher.
     * @throws Exception when configuration errors occurs.
     */
    @Bean
    public JobLauncher asyncLoadingJobLauncher(final JobRepository jobRepository) throws Exception {
        final TaskExecutorJobLauncher jobLauncher = new TaskExecutorJobLauncher();
        jobLauncher.setJobRepository(jobRepository);
        jobLauncher.setTaskExecutor(new SimpleAsyncTaskExecutor());
        jobLauncher.afterPropertiesSet();
        return jobLauncher;
    }

}
