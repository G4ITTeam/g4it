/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apibatchloading.business;

import com.soprasteria.g4it.backend.apibatchloading.exception.InventoryIntegrationRuntimeException;
import com.soprasteria.g4it.backend.apibatchloading.model.InventoryLoadingSession;
import com.soprasteria.g4it.backend.apiuser.business.OrganizationService;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import com.soprasteria.g4it.backend.common.filesystem.business.LocalFileStorage;
import com.soprasteria.g4it.backend.common.filesystem.model.FileSystem;
import com.soprasteria.g4it.backend.config.LoadingBatchConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.*;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryLoadingJobServiceTest {

    private final static String LOCAL_FILESYSTEM_PATH = "target/local-filestorage-test-loading";
    public final Long ORGANIZATION_ID = 1L;
    @Mock
    private JobLauncher asyncJobLauncher;
    @Mock
    private JobRepository jobRepository;
    @Mock
    private JobExplorer jobExplorer;
    @Mock
    private FileSystem fileSystem;
    @InjectMocks
    private InventoryLoadingJobService service;
    @Mock
    private OrganizationService organizationService;

    @BeforeEach
    public void beforeEach() {
        ReflectionTestUtils.setField(service, "localWorkingFolderBasePath", "./target/test-classes/");
    }

    @Test
    void whenCallLaunchInventoryLoading_thenReturnJobId() throws Exception {
        final String subscriber = "sub";
        final String organization = "org";
        final String inventoryName = "06-2023";
        final Long invenyoryId = 4L;

        final JobInstance instance = new JobInstance(1L, "testJob");
        final LocalDateTime startTime = LocalDateTime.now();
        final LocalDateTime endTime = LocalDateTime.now();
        final JobParameters param = new JobParametersBuilder()
                .addString("subscriber", subscriber)
                .addString("organization", organization).toJobParameters();
        final JobExecution execution = new JobExecution(instance, param);
        execution.setCreateTime(startTime);
        execution.setEndTime(endTime);
        execution.setExitStatus(ExitStatus.COMPLETED);
        execution.setStatus(BatchStatus.COMPLETED);

        when(fileSystem.mount(subscriber, ORGANIZATION_ID.toString())).thenReturn(new LocalFileStorage(LOCAL_FILESYSTEM_PATH));
        when(asyncJobLauncher.run(any(), any())).thenReturn(execution);

        final Long jobId = service.launchInventoryIntegration(InventoryLoadingSession
                .builder()
                .subscriber(subscriber)
                .inventoryName(inventoryName)
                .organization(organization)
                .organizationId(ORGANIZATION_ID)
                .sessionDate(new Date())
                .files(new ArrayList<>())
                .inventoryId(invenyoryId)
                .locale(Locale.ENGLISH)
                .build());

        assertThat(jobId).isEqualTo(1L);

        verify(asyncJobLauncher, times(1)).run(any(), any());
        verify(fileSystem, times(1)).mount(subscriber, ORGANIZATION_ID.toString());
    }

    @Test
    void whenAsyncJobLauncherThrowJobExecutionAlreadyRunningException_thenThrowInventoryEvaluationException() throws Exception {
        final String subscriber = "sub";
        final String organization = "org";
        final String inventoryName = "06-2023";
        final Long inventoryId = 4L;

        final LocalDateTime startTime = LocalDateTime.now();
        final LocalDateTime endTime = LocalDateTime.now();
        final JobExecution execution = new JobExecution(1L);
        execution.setCreateTime(startTime);
        execution.setEndTime(endTime);
        execution.setExitStatus(ExitStatus.COMPLETED);
        execution.setStatus(BatchStatus.COMPLETED);

        when(fileSystem.mount(subscriber, ORGANIZATION_ID.toString())).thenReturn(new LocalFileStorage(LOCAL_FILESYSTEM_PATH));
        when(asyncJobLauncher.run(any(), any())).thenThrow(new JobExecutionAlreadyRunningException(""));

        final InventoryLoadingSession session = InventoryLoadingSession
                .builder()
                .subscriber(subscriber)
                .inventoryName(inventoryName)
                .organization(organization)
                .organizationId(ORGANIZATION_ID)
                .sessionDate(new Date())
                .files(new ArrayList<>())
                .inventoryId(inventoryId)
                .locale(Locale.ENGLISH)
                .build();
        assertThatThrownBy(() -> service.launchInventoryIntegration(session))
                .hasMessageContaining("Job is already running.")
                .isInstanceOf(InventoryIntegrationRuntimeException.class);

        verify(asyncJobLauncher, times(1)).run(any(), any());
        verify(fileSystem, times(1)).mount(subscriber, ORGANIZATION_ID.toString());
    }

    @Test
    void whenAsyncJobLauncherThrowJobRestartException_thenThrowInventoryEvaluationException() throws Exception {
        final String subscriber = "sub";
        final String organization = "org";
        final String inventoryName = "06-2023";
        final Long inventoryId = 4L;

        final LocalDateTime startTime = LocalDateTime.now();
        final LocalDateTime endTime = LocalDateTime.now();
        final JobExecution execution = new JobExecution(1L);
        execution.setCreateTime(startTime);
        execution.setEndTime(endTime);
        execution.setExitStatus(ExitStatus.COMPLETED);
        execution.setStatus(BatchStatus.COMPLETED);

        when(fileSystem.mount(subscriber, ORGANIZATION_ID.toString())).thenReturn(new LocalFileStorage(LOCAL_FILESYSTEM_PATH));
        when(asyncJobLauncher.run(any(), any())).thenThrow(new JobRestartException(""));

        final InventoryLoadingSession session = InventoryLoadingSession
                .builder()
                .subscriber(subscriber)
                .inventoryName(inventoryName)
                .organization(organization)
                .organizationId(ORGANIZATION_ID)
                .sessionDate(new Date())
                .files(new ArrayList<>())
                .inventoryId(inventoryId)
                .locale(Locale.ENGLISH)
                .build();
        assertThatThrownBy(() -> service.launchInventoryIntegration(session))
                .hasMessageContaining("Illegal attempt at restarting Job.")
                .isInstanceOf(InventoryIntegrationRuntimeException.class);

        verify(asyncJobLauncher, times(1)).run(any(), any());
        verify(fileSystem, times(1)).mount(subscriber, ORGANIZATION_ID.toString());
    }

    @Test
    void whenAsyncJobLauncherThrowJobInstanceAlreadyCompleteException_thenThrowInventoryEvaluationException() throws Exception {
        final String subscriber = "sub";
        final String organization = "org";
        final String inventoryName = "06-2023";
        final Long inventoryId = 4L;

        final LocalDateTime startTime = LocalDateTime.now();
        final LocalDateTime endTime = LocalDateTime.now();
        final JobExecution execution = new JobExecution(1L);
        execution.setCreateTime(startTime);
        execution.setEndTime(endTime);
        execution.setExitStatus(ExitStatus.COMPLETED);
        execution.setStatus(BatchStatus.COMPLETED);

        when(fileSystem.mount(subscriber, ORGANIZATION_ID.toString())).thenReturn(new LocalFileStorage(LOCAL_FILESYSTEM_PATH));
        when(asyncJobLauncher.run(any(), any())).thenThrow(new JobInstanceAlreadyCompleteException(""));

        final InventoryLoadingSession session = InventoryLoadingSession
                .builder()
                .subscriber(subscriber)
                .inventoryName(inventoryName)
                .organization(organization)
                .organizationId(ORGANIZATION_ID)
                .sessionDate(new Date())
                .files(new ArrayList<>())
                .inventoryId(inventoryId)
                .locale(Locale.ENGLISH)
                .build();
        assertThatThrownBy(() -> service.launchInventoryIntegration(session))
                .hasMessageContaining("An instance of this Job already exists.")
                .isInstanceOf(InventoryIntegrationRuntimeException.class);

        verify(asyncJobLauncher, times(1)).run(any(), any());
        verify(fileSystem, times(1)).mount(subscriber, ORGANIZATION_ID.toString());
    }

    @Test
    void whenAsyncJobLauncherThrowJobParametersInvalidException_thenThrowInventoryEvaluationException() throws Exception {
        final String subscriber = "sub";
        final String organization = "org";
        final String inventoryName = "06-2023";

        final LocalDateTime startTime = LocalDateTime.now();
        final LocalDateTime endTime = LocalDateTime.now();
        final JobExecution execution = new JobExecution(1L);
        execution.setCreateTime(startTime);
        execution.setEndTime(endTime);
        execution.setExitStatus(ExitStatus.COMPLETED);
        execution.setStatus(BatchStatus.COMPLETED);

        when(fileSystem.mount(subscriber, ORGANIZATION_ID.toString())).thenReturn(new LocalFileStorage(LOCAL_FILESYSTEM_PATH));
        when(asyncJobLauncher.run(any(), any())).thenThrow(new JobParametersInvalidException(""));

        final InventoryLoadingSession session = InventoryLoadingSession
                .builder()
                .subscriber(subscriber)
                .inventoryName(inventoryName)
                .organization(organization)
                .organizationId(ORGANIZATION_ID)
                .sessionDate(new Date())
                .files(new ArrayList<>())
                .inventoryId(4L)
                .locale(Locale.ENGLISH)
                .build();
        assertThatThrownBy(() -> service.launchInventoryIntegration(session))
                .hasMessageContaining("Invalid parameters.")
                .isInstanceOf(InventoryIntegrationRuntimeException.class);

        verify(asyncJobLauncher, times(1)).run(any(), any());
        verify(fileSystem, times(1)).mount(subscriber, ORGANIZATION_ID.toString());
    }

    @Test
    void shouldRemoveJobInstanceForAnInventory() {
        // Given
        final Long inventoryId = 4L;
        final String organization = "org";

        // Build Job Instances
        final LocalDateTime startTime = LocalDateTime.now();
        final LocalDateTime endTime = LocalDateTime.now();

        // To remove
        final JobInstance instance1 = new JobInstance(1L, "testJob");
        final JobParameters param1 = new JobParametersBuilder().addLong("inventory.id", inventoryId).toJobParameters();
        final JobExecution execution1 = new JobExecution(instance1, param1);
        execution1.setCreateTime(startTime);
        execution1.setEndTime(endTime);
        execution1.setExitStatus(ExitStatus.COMPLETED);
        execution1.setStatus(BatchStatus.COMPLETED);
        execution1.setJobInstance(instance1);
        final JobInstance instance2 = new JobInstance(2L, "testJob");
        final JobParameters param2 = new JobParametersBuilder().addLong("inventory.id", inventoryId).toJobParameters();
        final JobExecution execution2 = new JobExecution(instance2, param2);
        execution2.setCreateTime(startTime);
        execution2.setEndTime(endTime);
        execution2.setExitStatus(ExitStatus.COMPLETED);
        execution2.setStatus(BatchStatus.COMPLETED);
        execution2.setJobInstance(instance2);

        // Not to remove
        final JobInstance instance3 = new JobInstance(3L, "testJob");
        final JobParameters param3 = new JobParametersBuilder().addLong("inventory.id", 15L).toJobParameters();
        final JobExecution execution3 = new JobExecution(instance3, param3);
        execution3.setCreateTime(startTime);
        execution3.setEndTime(endTime);
        execution3.setExitStatus(ExitStatus.COMPLETED);
        execution3.setStatus(BatchStatus.COMPLETED);
        execution3.setJobInstance(instance3);

        when(jobExplorer.findJobInstancesByJobName(LoadingBatchConfiguration.LOAD_INVENTORY_JOB, 0, Integer.MAX_VALUE)).thenReturn(List.of(instance1, instance2, instance3));
        when(jobExplorer.getJobExecutions(instance1)).thenReturn(List.of(execution1));
        when(jobExplorer.getJobExecutions(instance2)).thenReturn(List.of(execution2));
        when(jobExplorer.getJobExecutions(instance3)).thenReturn(List.of(execution3));
        doNothing().when(jobRepository).deleteJobExecution(any());
        doNothing().when(jobRepository).deleteJobInstance(any());

        service.deleteJobInstances(ORGANIZATION_ID, inventoryId);

        verify(jobExplorer, times(1)).findJobInstancesByJobName(LoadingBatchConfiguration.LOAD_INVENTORY_JOB, 0, Integer.MAX_VALUE);
        verify(jobExplorer, times(3)).getJobExecutions(any());
        verify(jobRepository, times(1)).deleteJobExecution(execution1);
        verify(jobRepository, times(1)).deleteJobExecution(execution2);
        verify(jobRepository, times(1)).deleteJobInstance(instance1);
        verify(jobRepository, times(1)).deleteJobInstance(instance2);
        verify(jobRepository, never()).deleteJobExecution(execution3);
        verify(jobRepository, never()).deleteJobInstance(instance3);
    }

    @Test
    void shouldRemoveJobInstanceForAnOrganization() {
        // Given
        final String organization = "org";
        final Organization linkedOrganization = Organization.builder().id(ORGANIZATION_ID).name(organization).build();

        // Build Job Instances
        final LocalDateTime startTime = LocalDateTime.now();
        final LocalDateTime endTime = LocalDateTime.now();

        // To remove
        final JobInstance instance1 = new JobInstance(1L, "testJob");
        final JobParameters param1 = new JobParametersBuilder().addString("organization", organization).toJobParameters();
        final JobExecution execution1 = new JobExecution(instance1, param1);
        execution1.setCreateTime(startTime);
        execution1.setEndTime(endTime);
        execution1.setExitStatus(ExitStatus.COMPLETED);
        execution1.setStatus(BatchStatus.COMPLETED);
        execution1.setJobInstance(instance1);
        final JobInstance instance2 = new JobInstance(2L, "testJob");
        final JobParameters param2 = new JobParametersBuilder().addString("organization", organization).toJobParameters();
        final JobExecution execution2 = new JobExecution(instance2, param2);
        execution2.setCreateTime(startTime);
        execution2.setEndTime(endTime);
        execution2.setExitStatus(ExitStatus.COMPLETED);
        execution2.setStatus(BatchStatus.COMPLETED);
        execution2.setJobInstance(instance2);

        // Not to remove
        final JobInstance instance3 = new JobInstance(3L, "testJob");
        final JobParameters param3 = new JobParametersBuilder().addString("organization", "unknown").toJobParameters();
        final JobExecution execution3 = new JobExecution(instance3, param3);
        execution3.setCreateTime(startTime);
        execution3.setEndTime(endTime);
        execution3.setExitStatus(ExitStatus.FAILED);
        execution3.setStatus(BatchStatus.FAILED);
        execution3.setJobInstance(instance3);

        when(organizationService.getOrganizationById(ORGANIZATION_ID)).thenReturn(linkedOrganization);
        when(jobExplorer.findJobInstancesByJobName(LoadingBatchConfiguration.LOAD_INVENTORY_JOB, 0, Integer.MAX_VALUE)).thenReturn(List.of(instance1, instance2, instance3));
        when(jobExplorer.getJobExecutions(instance1)).thenReturn(List.of(execution1));
        when(jobExplorer.getJobExecutions(instance2)).thenReturn(List.of(execution2));
        when(jobExplorer.getJobExecutions(instance3)).thenReturn(List.of(execution3));
        doNothing().when(jobRepository).deleteJobExecution(any());
        doNothing().when(jobRepository).deleteJobInstance(any());

        service.deleteJobInstances(ORGANIZATION_ID, null);

        verify(jobExplorer, times(1)).findJobInstancesByJobName(LoadingBatchConfiguration.LOAD_INVENTORY_JOB, 0, Integer.MAX_VALUE);
        verify(jobExplorer, times(3)).getJobExecutions(any());
        verify(jobRepository, times(1)).deleteJobExecution(execution1);
        verify(jobRepository, times(1)).deleteJobExecution(execution2);
        verify(jobRepository, times(1)).deleteJobInstance(instance1);
        verify(jobRepository, times(1)).deleteJobInstance(instance2);
        verify(jobRepository, never()).deleteJobExecution(execution3);
        verify(jobRepository, never()).deleteJobInstance(instance3);
    }

}
