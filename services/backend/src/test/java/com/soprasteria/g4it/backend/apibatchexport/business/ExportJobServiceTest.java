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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExportJobServiceTest {

    private final static String LOCAL_FILESYSTEM_PATH = "target/local-filestorage-test-export";

    @Mock
    private JobLauncher asyncJobLauncher;
    @Mock
    private JobRepository jobRepository;
    @Mock
    private JobExplorer jobExplorer;
    @InjectMocks
    private ExportJobService service;

    final static Long ORGANIZATION_ID = 1L;

    @BeforeEach
    public void beforeAll() {
        ReflectionTestUtils.setField(service, "localWorkingFolderBasePath", "./target/test-classes/");
    }

    @Test
    void whenCallLaunchExport_thenReturnJobId() throws Exception {
        final String subscriber = "sub";
        final String organization = "org";
        final String inventoryName = "02-2020";
        final String batchName = "batchName";
        final Long inventoryId = 4L;


        final JobInstance instance = new JobInstance(1L, "testJob");
        final LocalDateTime startTime = LocalDateTime.now();
        final LocalDateTime endTime = LocalDateTime.now();
        final JobParameters param = new JobParametersBuilder().addLong("inventory.id", inventoryId).toJobParameters();
        final JobExecution execution = new JobExecution(instance, param);
        execution.setCreateTime(startTime);
        execution.setEndTime(endTime);
        execution.setExitStatus(ExitStatus.COMPLETED);
        execution.setStatus(BatchStatus.COMPLETED);

        when(asyncJobLauncher.run(any(), any())).thenReturn(execution);

        final Long jobId = service.launchExport(subscriber, organization, inventoryId, inventoryName, batchName, ORGANIZATION_ID);

        assertThat(jobId).isEqualTo(1L);

        verify(asyncJobLauncher, times(1)).run(any(), any());
    }

    @Test
    void whenAsyncJobLauncherThrowJobExecutionAlreadyRunningException_thenThrowExportException() throws Exception {
        final String subscriber = "sub";
        final String organization = "org";
        final String inventoryName = "02-2020";
        final String batchName = "batchName";
        final Long inventoryId = 4L;

        final LocalDateTime startTime = LocalDateTime.now();
        final LocalDateTime endTime = LocalDateTime.now();
        final JobExecution execution = new JobExecution(1L);
        execution.setCreateTime(startTime);
        execution.setEndTime(endTime);
        execution.setExitStatus(ExitStatus.COMPLETED);
        execution.setStatus(BatchStatus.COMPLETED);

        when(asyncJobLauncher.run(any(), any())).thenThrow(new JobExecutionAlreadyRunningException(""));

        assertThatThrownBy(() -> service.launchExport(subscriber, organization, inventoryId, inventoryName, batchName, ORGANIZATION_ID))
                .hasMessageContaining("Job is already running.")
                .isInstanceOf(ExportRuntimeException.class);

        verify(asyncJobLauncher, times(1)).run(any(), any());
    }

    @Test
    void whenAsyncJobLauncherThrowJobRestartException_thenThrowInventoryEvaluationException() throws Exception {
        final String subscriber = "sub";
        final String organization = "org";
        final String inventoryName = "02-2020";
        final String batchName = "batchName";
        final Long inventoryId = 4L;

        final LocalDateTime startTime = LocalDateTime.now();
        final LocalDateTime endTime = LocalDateTime.now();
        final JobExecution execution = new JobExecution(1L);
        execution.setCreateTime(startTime);
        execution.setEndTime(endTime);
        execution.setExitStatus(ExitStatus.COMPLETED);
        execution.setStatus(BatchStatus.COMPLETED);

        when(asyncJobLauncher.run(any(), any())).thenThrow(new JobRestartException(""));

        assertThatThrownBy(() -> service.launchExport(subscriber, organization, inventoryId, inventoryName, batchName, ORGANIZATION_ID))
                .hasMessageContaining("Illegal attempt at restarting Job.")
                .isInstanceOf(ExportRuntimeException.class);

        verify(asyncJobLauncher, times(1)).run(any(), any());
    }

    @Test
    void whenAsyncJobLauncherThrowJobInstanceAlreadyCompleteException_thenThrowInventoryEvaluationException() throws Exception {
        final String subscriber = "sub";
        final String organization = "org";
        final String inventoryName = "02-2020";
        final String batchName = "batchName";
        final Long inventoryId = 4L;

        final LocalDateTime startTime = LocalDateTime.now();
        final LocalDateTime endTime = LocalDateTime.now();
        final JobExecution execution = new JobExecution(1L);
        execution.setCreateTime(startTime);
        execution.setEndTime(endTime);
        execution.setExitStatus(ExitStatus.COMPLETED);
        execution.setStatus(BatchStatus.COMPLETED);

        when(asyncJobLauncher.run(any(), any())).thenThrow(new JobInstanceAlreadyCompleteException(""));

        assertThatThrownBy(() -> service.launchExport(subscriber, organization, inventoryId, inventoryName, batchName, ORGANIZATION_ID))
                .hasMessageContaining("An instance of this Job already exists.")
                .isInstanceOf(ExportRuntimeException.class);

        verify(asyncJobLauncher, times(1)).run(any(), any());
    }

    @Test
    void whenAsyncJobLauncherThrowJobParametersInvalidException_thenThrowInventoryEvaluationException() throws Exception {
        final String subscriber = "sub";
        final String organization = "org";
        final String inventoryName = "02-2020";
        final String batchName = "batchName";
        final Long inventoryId = 4L;

        final LocalDateTime startTime = LocalDateTime.now();
        final LocalDateTime endTime = LocalDateTime.now();
        final JobExecution execution = new JobExecution(1L);
        execution.setCreateTime(startTime);
        execution.setEndTime(endTime);
        execution.setExitStatus(ExitStatus.COMPLETED);
        execution.setStatus(BatchStatus.COMPLETED);

        when(asyncJobLauncher.run(any(), any())).thenThrow(new JobParametersInvalidException(""));

        assertThatThrownBy(() -> service.launchExport(subscriber, organization, inventoryId, inventoryName, batchName, ORGANIZATION_ID))
                .hasMessageContaining("Invalid parameters.")
                .isInstanceOf(ExportRuntimeException.class);

        verify(asyncJobLauncher, times(1)).run(any(), any());
    }

    @Test
    void shouldRemoveJobInstanceForAnInventory() throws Exception {
        // Given
        final Long inventoryId = 4L;

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

        when(jobExplorer.findJobInstancesByJobName(ExportBatchConfiguration.EXPORT_JOB, 0, Integer.MAX_VALUE)).thenReturn(List.of(instance1, instance2, instance3));
        when(jobExplorer.getJobExecutions(instance1)).thenReturn(List.of(execution1));
        when(jobExplorer.getJobExecutions(instance2)).thenReturn(List.of(execution2));
        when(jobExplorer.getJobExecutions(instance3)).thenReturn(List.of(execution3));
        doNothing().when(jobRepository).deleteJobExecution(any());
        doNothing().when(jobRepository).deleteJobInstance(any());

        service.deleteJobInstances(inventoryId);

        verify(jobExplorer, times(1)).findJobInstancesByJobName(ExportBatchConfiguration.EXPORT_JOB, 0, Integer.MAX_VALUE);
        verify(jobExplorer, times(3)).getJobExecutions(any());
        verify(jobRepository, times(1)).deleteJobExecution(execution1);
        verify(jobRepository, times(1)).deleteJobExecution(execution2);
        verify(jobRepository, times(1)).deleteJobInstance(instance1);
        verify(jobRepository, times(1)).deleteJobInstance(instance2);
        verify(jobRepository, never()).deleteJobExecution(execution3);
        verify(jobRepository, never()).deleteJobInstance(instance3);
    }

}
