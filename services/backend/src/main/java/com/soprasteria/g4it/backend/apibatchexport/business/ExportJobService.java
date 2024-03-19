/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apibatchexport.business;

import com.soprasteria.g4it.backend.apibatchexport.config.ExportBatchConfiguration;
import com.soprasteria.g4it.backend.apibatchexport.exception.ExportRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

/**
 * Export service.
 */
@Slf4j
@Service
public class ExportJobService {

    /**
     * Inventory identifier job param identifier.
     */
    public static final String INVENTORY_ID_JOB_PARAM = "inventory.id";
    public static final String INVENTORY_NAME_JOB_PARAM = "inventory.name";
    public static final String BATCH_NAME_JOB_PARAM = "batch.name";
    public static final String ORGANIZATION_JOB_PARAM = "organization";

    public static final String SUBSCRIBER_JOB_PARAM = "subscriber";

    /**
     * Async Job Launcher.
     */
    @Autowired
    private JobLauncher asyncExportJobLauncher;

    /**
     * Repository to access spring batch metadata.
     */
    @Autowired
    private JobRepository jobRepository;

    /**
     * The Spring JobExplorer
     */
    @Autowired
    private JobExplorer explorer;

    /**
     * Job to launch.
     */
    @Autowired
    private Job exportJob;

    /**
     * Local working folder.
     */
    @Value("${batch.local.working.folder.base.path:}")
    private String localWorkingFolderBasePath;

    /**
     * Launch Batch.
     *
     * @param subscriber    the client subscriber.
     * @param organization  the subscriber's organization.
     * @param inventoryId   the inventory id.
     * @param inventoryName the inventory name.
     * @param username      the username.
     * @param batchName     the calculation batch name.
     * @return the job id.
     * @throws ExportRuntimeException when error occurs in batch job.
     */
    public Long launchExport(final String subscriber, final String organization, final Long inventoryId, final String inventoryName, final String batchName, final String username) throws ExportRuntimeException {
        try {
            // trigger job execution
            final JobExecution jobExecution = asyncExportJobLauncher.run(exportJob,
                    new JobParametersBuilder()
                            .addString("local.working.folder", getRandomFolderName())
                            .addLong(INVENTORY_ID_JOB_PARAM, inventoryId)
                            .addString(INVENTORY_NAME_JOB_PARAM, inventoryName)
                            .addString("username", username)
                            .addString(BATCH_NAME_JOB_PARAM, batchName)
                            .addString(SUBSCRIBER_JOB_PARAM, subscriber)
                            .addString(ORGANIZATION_JOB_PARAM, organization)
                            .toJobParameters());
            return jobExecution.getJobId();
        } catch (final JobExecutionAlreadyRunningException e) {
            log.error("JobExecutionAlreadyRunningException : ", e);
            throw new ExportRuntimeException("Job is already running.");
        } catch (final JobRestartException e) {
            log.error("JobRestartException : ", e);
            throw new ExportRuntimeException("Illegal attempt at restarting Job.");
        } catch (final JobInstanceAlreadyCompleteException e) {
            log.error("JobInstanceAlreadyCompleteException : ", e);
            throw new ExportRuntimeException("An instance of this Job already exists.");
        } catch (final JobParametersInvalidException e) {
            log.error("JobParametersInvalidException : ", e);
            throw new ExportRuntimeException("Invalid parameters.");
        }
    }

    /**
     * Remove job instances for an organization and optionally an inventoryName.
     *
     * @param inventoryId the inventory id (Optional).
     */
    public void deleteJobInstances(final Long inventoryId) {
        // Get all evaluate inventory.
        final List<JobInstance> runningJobExecutions = explorer.findJobInstancesByJobName(ExportBatchConfiguration.EXPORT_JOB, 0, Integer.MAX_VALUE);

        // Extract job executions to remove.
        final List<JobExecution> jobExecutionsToRemove = runningJobExecutions
                .stream()
                .map(explorer::getJobExecutions)
                .flatMap(List::stream).toList()
                .stream()
                .filter(jobExecution -> inventoryId.equals(jobExecution.getJobParameters().getLong(INVENTORY_ID_JOB_PARAM)))
                .toList();
        // Extact job instances to remove.
        final List<JobInstance> jobInstancesToRemove = jobExecutionsToRemove.stream().map(JobExecution::getJobInstance).distinct().toList();

        // Remove.
        jobExecutionsToRemove.forEach(jobRepository::deleteJobExecution);
        jobInstancesToRemove.forEach(jobRepository::deleteJobInstance);
    }

    private String getRandomFolderName() {
        // We generate random folder name
        return Paths.get(localWorkingFolderBasePath, UUID.randomUUID().toString()).toString();
    }
}
