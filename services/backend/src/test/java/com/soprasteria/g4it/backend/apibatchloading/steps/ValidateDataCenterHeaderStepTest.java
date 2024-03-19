/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apibatchloading.steps;

import com.soprasteria.g4it.backend.apibatchloading.steps.common.tasklet.ValidateHeaderTasklet;
import com.soprasteria.g4it.backend.common.filesystem.model.FileFolder;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static com.soprasteria.g4it.backend.apibatchloading.model.InventoryJobParams.ORGANIZATION_JOB_PARAM;
import static com.soprasteria.g4it.backend.apibatchloading.model.InventoryJobParams.SUBSCRIBER_JOB_PARAM;


@SpringBootTest
@SpringBatchTest
@ActiveProfiles({"local", "test"})
@DirtiesContext
class ValidateDataCenterHeaderStepTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JobLauncher jobLauncher;
    @Autowired
    @Qualifier("loadInventoryJob")
    private Job job;

    @Value("${filesystem.local.path}")
    private String fileSystemBasePath;
    private static final Path testFolder = Path.of("src/test/resources/apibatchloading/work");

    @BeforeEach
    public void beforeEach() {
        this.jobLauncherTestUtils.setJobLauncher(jobLauncher);
        this.jobLauncherTestUtils.setJob(job);
    }

    @Test
    void shouldSkippedIfInvalidFileHeader() throws Exception {
        final String subscriber = "SSG";
        final String organization = "local";

        // given an existing input folder
        final Calendar calendar = Calendar.getInstance();
        calendar.set(2023, Calendar.JANUARY, 01, 10, 45, 59);
        final String formattedSessionDate = new SimpleDateFormat("yyyyMMdd-HHmmss").format(calendar.getTime());

        // Copy input file to work folder.
        FileSystemUtils.copyRecursively(new File(testFolder.resolve(formattedSessionDate).toString()),
                new File(Path.of(fileSystemBasePath, subscriber, organization, FileFolder.WORK.getFolderName(),
                        formattedSessionDate).toString()));

        final ExecutionContext context = new ExecutionContext();
        context.putString("session.path", "20230101-104559");
        context.putLong("inventory.id", 1);

        final JobParameters jobParameters = new JobParametersBuilder()
                .addString(SUBSCRIBER_JOB_PARAM, subscriber)
                .addString(ORGANIZATION_JOB_PARAM, organization)
                .addDate("session.date", calendar.getTime())
                .toJobParameters();

        // when we execute the step
        // it should throw an InvalidHeaderException
        final JobExecution execution = jobLauncherTestUtils.launchStep("validateDataCenterHeaderStep", jobParameters, context);

        // Assertions.
        Assertions.assertThat(execution.getExitStatus().getExitCode()).isEqualTo(ValidateHeaderTasklet.SKIPPED_EXIT_STATUS);
    }

}
