/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apibatchloading.business;

import com.soprasteria.g4it.backend.apibatchloading.exception.InventoryIntegrationRuntimeException;
import com.soprasteria.g4it.backend.apibatchloading.exception.InventoryLoadingException;
import com.soprasteria.g4it.backend.apibatchloading.model.InventoryJobParams;
import com.soprasteria.g4it.backend.apibatchloading.model.InventoryLoadingSession;
import com.soprasteria.g4it.backend.apiuser.business.OrganizationService;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import com.soprasteria.g4it.backend.common.filesystem.business.FileStorage;
import com.soprasteria.g4it.backend.common.filesystem.business.FileSystem;
import com.soprasteria.g4it.backend.common.filesystem.model.FileDescription;
import com.soprasteria.g4it.backend.common.filesystem.model.FileFolder;
import com.soprasteria.g4it.backend.config.LoadingBatchConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

@Service
@Slf4j
public class InventoryLoadingJobService {

    /**
     * Class Logger.
     */
    public static final String ORGANIZATION = "organization";
    public static final String ORGANIZATION_ID = "organization.id";
    public static final String INVENTORY_ID_JOB_PARAM = "inventory.id";
    /**
     * Async Job Launcher.
     */
    @Autowired
    private JobLauncher asyncLoadingJobLauncher;
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
    private Job loadInventoryJob;
    @Autowired
    private FileSystem fileSystem;
    @Autowired
    private OrganizationService organizationService;
    /**
     * Local working folder.
     */
    @Value("${batch.local.working.folder.base.path:}")
    private String localWorkingFolderBasePath;

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

    /**
     * Launch the loading batch job.
     *
     * @param session session information.
     * @return batch instance id.
     */
    public Long launchInventoryIntegration(final InventoryLoadingSession session) {
        try {
            // Move files to dedicated work folder
            this.prepareWorkingFolder(session);
            // trigger job execution
            final JobExecution jobExecution = asyncLoadingJobLauncher.run(loadInventoryJob,
                    InventoryJobParams.builder()
                            .subscriber(session.getSubscriber())
                            .organization(session.getOrganization())
                            .organizationId(session.getOrganizationId())
                            .sessionDate(session.getSessionDate())
                            .inventoryId(session.getInventoryId())
                            .localWorkingFolderBasePath(localWorkingFolderBasePath)
                            .inventoryName(session.getInventoryName())
                            .locale(session.getLocale())
                            .build().toJobParams());
            return jobExecution.getJobId();
        } catch (JobExecutionAlreadyRunningException e) {
            log.error("JobExecutionAlreadyRunningException : ", e);
            throw new InventoryIntegrationRuntimeException("Job is already running.");
        } catch (JobRestartException e) {
            log.error("JobRestartException : ", e);
            throw new InventoryIntegrationRuntimeException("Illegal attempt at restarting Job.");
        } catch (JobInstanceAlreadyCompleteException e) {
            log.error("JobInstanceAlreadyCompleteException : ", e);
            throw new InventoryIntegrationRuntimeException("An instance of this Job already exists.");
        } catch (JobParametersInvalidException e) {
            log.error("JobParametersInvalidException : ", e);
            throw new InventoryIntegrationRuntimeException("Invalid parameters.");
        }
    }

    private void prepareWorkingFolder(final InventoryLoadingSession session) throws InventoryLoadingException {
        final FileStorage storage = fileSystem.mount(session.getSubscriber(), session.getOrganizationId().toString());

        if (storage == null) {
            throw new InventoryLoadingException("Can't mount storage for organization " + session.getOrganizationId());
        }

        for (final FileDescription file : session.getFiles()) {
            try {
                storage.moveAndRename(FileFolder.INPUT, FileFolder.WORK, file.getName(), filePath(session, file));
            } catch (IOException e) {
                throw new InventoryLoadingException("An error occured while preparing working folder", e);
            }
        }
    }

    private String filePath(final InventoryLoadingSession session, final FileDescription file) {
        return String.format("%s/%s/%s", session.getSessionPath(), file.getType().name(), file.getName());
    }

    /**
     * Remove job instances for an organization and optionally an inventoryId.
     *
     * @param organizationId the organization's id.
     * @param inventoryId    the inventory id (Optional).
     */
    public void deleteJobInstances(final Long organizationId, final Long inventoryId) {
        final Organization linkedOrganization = organizationService.getOrganizationById(organizationId);
        // Get all evaluate inventory.
        final List<JobInstance> runningJobExecutions = explorer.findJobInstancesByJobName(LoadingBatchConfiguration.LOAD_INVENTORY_JOB, 0, Integer.MAX_VALUE);

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


}
