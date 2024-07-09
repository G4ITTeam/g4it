/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apibatchevaluation.business;

import com.soprasteria.g4it.backend.apibatchevaluation.exception.InventoryEvaluationRuntimeException;
import com.soprasteria.g4it.backend.apiuser.business.OrganizationService;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import com.soprasteria.g4it.backend.config.EvaluationBatchConfiguration;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;

@Service
public class InventoryEvaluationJobService {

    /**
     * Class Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(InventoryEvaluationJobService.class);

    /**
     * Organization parameter name.
     */
    public static final String ORGANIZATION = "organization";

    /**
     * Inventory identifier job param identifier.
     */
    public static final String INVENTORY_ID_JOB_PARAM = "inventory.id";
    public static final String BATCH_NAME_JOB_PARAM = "batch.name";
    public static final String INVENTORY_NAME_JOB_PARAM = "inventory.name";

    /**
     * Async Job Launcher.
     */
    @Autowired
    private JobLauncher asyncEvaluationJobLauncher;

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
    private Job evaluateInventoryJob;

    /**
     * Local working folder.
     */
    @Value("${batch.local.working.folder.base.path:}")
    private String localWorkingFolderBasePath;

    public static final String ORGANIZATION_ID = "organization.id";

    @Autowired
    private OrganizationService organizationService;

    /**
     * Launch Batch.
     *
     * @param organization   the organization.
     * @param inventoryName  the inventory name.
     * @param inventoryId    the inventory's id.
     * @param organizationId the organization's id.
     * @return the job id.
     * @throws InventoryEvaluationRuntimeException when error occurs in batch job.
     */
    public Long launchInventoryEvaluation(final String organization, final String inventoryName, final Long inventoryId, final Long organizationId) throws InventoryEvaluationRuntimeException {
        try {
            // trigger job execution
            final JobExecution jobExecution = asyncEvaluationJobLauncher.run(evaluateInventoryJob,
                    new JobParametersBuilder()
                            .addString("local.working.folder", getRandomFolderName())
                            .addString(ORGANIZATION, organization)
                            .addLong(ORGANIZATION_ID, organizationId)
                            .addLong(INVENTORY_ID_JOB_PARAM, inventoryId)
                            .addDate("processing.date", new Date())
                            .addString(BATCH_NAME_JOB_PARAM, UUID.randomUUID().toString())
                            .addString(INVENTORY_NAME_JOB_PARAM, inventoryName)
                            .toJobParameters());
            return jobExecution.getJobId();
        } catch (final JobExecutionAlreadyRunningException e) {
            LOGGER.error("JobExecutionAlreadyRunningException : ", e);
            throw new InventoryEvaluationRuntimeException("Job is already running.");
        } catch (final JobRestartException e) {
            LOGGER.error("JobRestartException : ", e);
            throw new InventoryEvaluationRuntimeException("Illegal attempt at restarting Job.");
        } catch (final JobInstanceAlreadyCompleteException e) {
            LOGGER.error("JobInstanceAlreadyCompleteException : ", e);
            throw new InventoryEvaluationRuntimeException("An instance of this Job already exists.");
        } catch (final JobParametersInvalidException e) {
            LOGGER.error("JobParametersInvalidException : ", e);
            throw new InventoryEvaluationRuntimeException("Invalid parameters.");
        }
    }


    private String getRandomFolderName() {
        // We generate random folder name
        return Paths.get(localWorkingFolderBasePath, UUID.randomUUID().toString()).toString();
    }

    /**
     * Remove job instances for an organization and optionally an inventoryName.
     *
     * @param organizationId the organization's id.
     * @param inventoryId    the inventory id (Optional).
     */
    public void deleteJobInstances(final Long organizationId, final Long inventoryId) {

        final Organization linkedOrganization = organizationService.getOrganizationById(organizationId);

        // Get all evaluate inventory.
        final List<JobInstance> runningJobExecutions = explorer.findJobInstancesByJobName(EvaluationBatchConfiguration.EVALUATE_INVENTORY_JOB, 0, Integer.MAX_VALUE);

        final Predicate<JobExecution> jobExecutionPredicate = getJobExecutionPredicate(linkedOrganization != null ? linkedOrganization.getName() : null, organizationId, inventoryId);

        // Extract job executions to remove.
        final List<JobExecution> jobExecutionsToRemove = runningJobExecutions
                .stream()
                .map(explorer::getJobExecutions)
                .flatMap(List::stream).toList()
                .stream()
                .filter(jobExecutionPredicate).toList();
        // Extact job instances to remove.
        final List<JobInstance> jobInstancesToRemove = jobExecutionsToRemove.stream().map(JobExecution::getJobInstance).distinct().toList();

        // Remove.
        jobExecutionsToRemove.forEach(jobRepository::deleteJobExecution);
        jobInstancesToRemove.forEach(jobRepository::deleteJobInstance);
    }

    private static Predicate<JobExecution> getJobExecutionPredicate(String organization, Long organizationId, Long inventoryId) {
        final Predicate<JobExecution> jobExecutionPredicate;
        if (inventoryId == null) {
            // Get job instance for an organization.
            jobExecutionPredicate = jobExecution -> {
                if (!ObjectUtils.isEmpty(jobExecution.getJobParameters().getLong(ORGANIZATION_ID)))
                    return Objects.equals(organizationId, jobExecution.getJobParameters().getLong(ORGANIZATION_ID));
                else
                    return StringUtils.equals(organization, jobExecution.getJobParameters().getString(ORGANIZATION));
            };
        } else {
            // Get job instance for an inventory.
            jobExecutionPredicate = jobExecution -> inventoryId.equals(jobExecution.getJobParameters().getLong(INVENTORY_ID_JOB_PARAM));
        }
        return jobExecutionPredicate;
    }
}
