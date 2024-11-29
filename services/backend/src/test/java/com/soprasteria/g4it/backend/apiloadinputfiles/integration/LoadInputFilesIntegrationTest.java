/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.integration;

import com.soprasteria.g4it.backend.TestUtils;
import com.soprasteria.g4it.backend.apiinout.business.InApplicationService;
import com.soprasteria.g4it.backend.apiinout.business.InDatacenterService;
import com.soprasteria.g4it.backend.apiinout.business.InPhysicalEquipmentService;
import com.soprasteria.g4it.backend.apiinout.business.InVirtualEquipmentService;
import com.soprasteria.g4it.backend.apiinout.repository.InApplicationRepository;
import com.soprasteria.g4it.backend.apiinout.repository.InDatacenterRepository;
import com.soprasteria.g4it.backend.apiinout.repository.InPhysicalEquipmentRepository;
import com.soprasteria.g4it.backend.apiinout.repository.InVirtualEquipmentRepository;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.apiinventory.repository.InventoryRepository;
import com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.AsyncLoadFilesService;
import com.soprasteria.g4it.backend.apiloadinputfiles.controller.LoadInputFilesController;
import com.soprasteria.g4it.backend.apireferential.business.ReferentialImportService;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import com.soprasteria.g4it.backend.apiuser.modeldb.Subscriber;
import com.soprasteria.g4it.backend.apiuser.repository.OrganizationRepository;
import com.soprasteria.g4it.backend.apiuser.repository.SubscriberRepository;
import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.task.model.TaskStatus;
import com.soprasteria.g4it.backend.common.task.model.TaskType;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.server.gen.api.dto.TaskIdRest;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;
import org.zeroturnaround.zip.ZipUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;

@SpringBootTest
@ActiveProfiles({"local", "test"})
@Slf4j
class LoadInputFilesIntegrationTest {

    @Autowired
    LoadInputFilesController loadInputFilesController;

    @Autowired
    AsyncLoadFilesService asyncLoadFilesService;

    @Autowired
    SubscriberRepository subscriberRepository;
    @Autowired
    OrganizationRepository organizationRepository;
    @Autowired
    InventoryRepository inventoryRepository;
    @Autowired
    TaskRepository taskRepository;
    @Autowired
    ReferentialImportService referentialImportService;

    @Autowired
    InDatacenterService inDatacenterService;
    @Autowired
    InPhysicalEquipmentService inPhysicalEquipmentService;
    @Autowired
    InVirtualEquipmentService inVirtualEquipmentService;
    @Autowired
    InApplicationService inApplicationService;

    @Autowired
    InDatacenterRepository inDatacenterRepository;
    @Autowired
    InPhysicalEquipmentRepository inPhysicalEquipmentRepository;
    @Autowired
    InVirtualEquipmentRepository inVirtualEquipmentRepository;
    @Autowired
    InApplicationRepository inApplicationRepository;

    private static final String SUBSCRIBER = "SUBSCRIBER";

    private static final Path apiloadinputfiles = Path.of("src/test/resources/apiloadinputfiles");

    // Set to true if you want Assertions on each fail
    // please commit with  SHOW_ASSERTION = false;
    private static final boolean SHOW_ASSERTION = false;

    /**
     * Execute all test cases located in src/test/resources/apiloadinputfiles
     */
    @Transactional
    @Test
    void loadInputFilesTest() throws IOException {

        Locale.setDefault(Locale.ENGLISH);

        var subscriber = subscriberRepository.save(Subscriber.builder().name(SUBSCRIBER).build());
        var organization = organizationRepository.save(Organization.builder().subscriber(subscriber).name("DEMO").build());

        taskRepository.deleteAll();
        inventoryRepository.deleteAll();

        var inventory = inventoryRepository.save(Inventory.builder()
                .name("TEST LOAD INPUT FILES")
                .organization(organization).build());

        // case with no input data
        ResponseEntity<TaskIdRest> response = loadInputFilesController.launchloadInputFiles(SUBSCRIBER, 1L, 1L, "fr", null, null, null, null);
        Assertions.assertNull(response.getBody().getTaskId());

        final Path targetInputFiles = Path.of("target/local-filesystem").resolve(SUBSCRIBER).resolve(String.valueOf(organization.getId())).resolve("input");
        Files.createDirectories(targetInputFiles);

        final Path targetOutputFiles = Path.of("target/local-filesystem").resolve(SUBSCRIBER).resolve(String.valueOf(organization.getId())).resolve("output");
        FileSystemUtils.deleteRecursively(targetOutputFiles);

        LocalDateTime fixedDateTime = LocalDateTime.of(2024, 1, 1, 0, 0, 0);
        Context context = Context.builder()
                .subscriber("SUBSCRIBER")
                .organizationId(organization.getId())
                .organizationName("DEMO")
                .inventoryId(inventory.getId())
                .locale(Locale.getDefault())
                .datetime(fixedDateTime)
                .hasApplications(true).hasVirtualEquipments(true)
                .build();

        boolean allOk = true;
        for (File testFolder : Arrays.stream(Objects.requireNonNull(apiloadinputfiles.toFile().listFiles())).sorted().toList()) {
            var testCase = testFolder.getName();

            // clean tables
            inDatacenterRepository.deleteAll();
            inPhysicalEquipmentRepository.deleteAll();
            inVirtualEquipmentRepository.deleteAll();
            inApplicationRepository.deleteAll();

            // copy files in work
            File inputFolder = apiloadinputfiles.resolve(testCase).resolve("input").toFile();
            List<String> filenames = new ArrayList<>();

            for (File f : Objects.requireNonNull(inputFolder.listFiles())) {
                String targetFilename = f.getName().replace(".csv", ".csv_" + UUID.randomUUID() + ".csv");
                Files.copy(f.toPath(), targetInputFiles.resolve(targetFilename));
                filenames.add(targetFilename);
            }

            File refFolder = apiloadinputfiles.resolve(testCase).resolve("ref").toFile();
            for (File f : Objects.requireNonNull(refFolder.listFiles())) {
                MultipartFile multipartFile = new MockMultipartFile("file", f.getName(), "text/csv", Files.readAllBytes(f.toPath()));
                referentialImportService.importReferentialCSV(f.getName().replace(".csv", ""), multipartFile, null);
            }

            var task = taskRepository.save(createTask(context, filenames));
            asyncLoadFilesService.execute(context, task);

            Path outputPath = apiloadinputfiles.resolve(testCase).resolve("output");

            for (File file : Objects.requireNonNull(outputPath.toFile().listFiles())) {
                var actual = switch (file.getName()) {
                    case "1-datacenters.json" ->
                            TestUtils.toJson(inDatacenterService.getByInventory(context.getInventoryId()), "inventoryId");
                    case "2-physical_equipments.json" ->
                            TestUtils.toJson(inPhysicalEquipmentService.getByInventory(context.getInventoryId()), "inventoryId");
                    case "3-virtual_equipments.json" ->
                            TestUtils.toJson(inVirtualEquipmentService.getByInventory(context.getInventoryId()), "inventoryId");
                    case "4-applications.json" ->
                            TestUtils.toJson(inApplicationService.getByInventory(context.getInventoryId()), "inventoryId");
                    default -> "[]";
                };
                var expected = TestUtils.formatJson(Files.readString(outputPath.resolve(file.getName())), "inventoryId");

                if (actual.equals(expected)) {
                    log.info("Test case: '{}' - OK Assert file {}", testCase, "output/" + file.getName());
                } else {
                    allOk = false;
                    log.error("Test case: '{}' - KO: Assert file {}", testCase, "output/" + file.getName());
                    System.out.println("ACTUAL: ");
                    System.out.println(actual);
                    System.out.println("EXPECTED: ");
                    System.out.println(expected);
                }
            }

            Path zipDirPath = Path.of("target/local-filesystem").resolve("SUBSCRIBER").resolve(String.valueOf(organization.getId())).resolve("output").resolve(String.valueOf(task.getId()));
            Path zipPath = zipDirPath.resolve(Constants.REJECTED_FILES_ZIP);

            if (Files.exists(zipPath)) {
                ZipUtil.unpack(zipPath.toFile(), zipDirPath.resolve("out").toFile());
                File[] rejectFiles = zipDirPath.resolve("out").toFile().listFiles();
                for (File file : rejectFiles) {
                    var actual = Files.readString(file.toPath()).replaceAll("\r\n", "\n");
                    var expected = Files.readString(apiloadinputfiles.resolve(testCase).resolve("rejects").resolve(file.getName())).replaceAll("\r\n", "\n");
                    if (actual.equals(expected)) {
                        log.info("Test case: '{}' - OK Assert file {}", testCase, "rejects/" + file.getName());
                    } else {
                        allOk = false;
                        log.error("Test case: '{}' - KO: Assert file {}", testCase, "rejects/" + file.getName());
                        System.out.println("ACTUAL: ");
                        System.out.println(actual);
                        System.out.println("EXPECTED: ");
                        System.out.println(expected);
                        if (SHOW_ASSERTION) Assertions.assertEquals(expected, actual);
                    }
                }
            } else {
                log.info("Test case: '{}' does not have rejects", testCase);
            }

            FileSystemUtils.deleteRecursively(zipPath);

        }

        if (!allOk) {
            Assertions.fail("At least one test case has fail, please check the logs in the console");
        }

    }

    private Task createTask(Context context, List<String> filenames) {
        return Task.builder()
                .creationDate(context.getDatetime())
                .details(new ArrayList<>())
                .lastUpdateDate(context.getDatetime())
                .progressPercentage("0%")
                .status(TaskStatus.TO_START.toString())
                .type(TaskType.LOADING.toString())
                .inventory(Inventory.builder().id(context.getInventoryId()).build())
                .filenames(filenames)
                .build();
    }
}
