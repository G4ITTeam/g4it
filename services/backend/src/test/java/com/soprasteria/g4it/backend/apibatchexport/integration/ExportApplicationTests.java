/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apibatchexport.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.io.File;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for Inventory Evaluation Batch.
 */
@SpringBootTest
@SpringBatchTest
@ActiveProfiles({"local", "test"}) // Order is important, test override local filesystem configuration
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Sql("/sql/data.sql")
class ExportApplicationTests {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Value("${filesystem.local.path}")
    private String fileSystemLocalPath;

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    @Qualifier("exportJob")
    private Job job;

    @MockBean
    private CacheManager cacheManager;

    @BeforeEach
    public void beforeEach() {
        this.jobLauncherTestUtils.setJobLauncher(jobLauncher);
        this.jobLauncherTestUtils.setJob(job);
    }

    /**
     * Cas de test complet :
     * <p>
     * La base de données contient l'ensemble des données valides pour intégration dans numEcoEval.
     * <p>
     * Attendu :
     * 1. Le traitement fini COMPLETED
     * 2. Les données de reporting en retour de numEcoEval sont bien insérées en base de données.
     * 3. Les données de reporting.sont bien liées à un inventaire.
     *
     * @throws Exception
     */
    @Test
    void givenReferenceInput_whenJobExecuted_thenSuccess() throws Exception {
        final String subscriber = "SSG";
        final String organization = "local";

        final String WORKING_FOLDER = String.join(File.separator, fileSystemLocalPath, "local_folder_1");

        // when
        final JobExecution jobExecution = this.jobLauncherTestUtils.launchJob(new JobParametersBuilder()
                .addString("local.working.folder", WORKING_FOLDER)
                .addString("delete.local.working.folder", "true")
                .addLong("inventory.id", 1L)
                .addString("inventory.date", "03-2022")
                .addString("organization", organization)
                .addString("subscriber", subscriber)
                .toJobParameters());

        // General Assertions
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        final Resource outputPath = new ClassPathResource(WORKING_FOLDER);
        assertThat(outputPath.exists()).isFalse();
        final File outputExportPath = Path.of(fileSystemLocalPath).resolve(subscriber).resolve(organization).resolve("export").toFile();
        assertThat(outputExportPath).isNotEmptyDirectory();
    }

}
