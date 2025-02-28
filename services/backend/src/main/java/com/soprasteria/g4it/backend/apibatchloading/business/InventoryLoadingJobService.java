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
import com.soprasteria.g4it.backend.apiloadinputfiles.business.LoadInputFilesService;
import com.soprasteria.g4it.backend.apiuser.business.OrganizationService;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import com.soprasteria.g4it.backend.common.filesystem.business.FileStorage;
import com.soprasteria.g4it.backend.common.filesystem.business.FileSystem;
import com.soprasteria.g4it.backend.common.filesystem.business.InputStreamMultipartFile;
import com.soprasteria.g4it.backend.common.filesystem.model.*;
import com.soprasteria.g4it.backend.common.task.model.TaskStatus;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import com.soprasteria.g4it.backend.common.utils.CsvUtils;
import com.soprasteria.g4it.backend.common.utils.InfrastructureType;
import com.soprasteria.g4it.backend.config.LoadingBatchConfiguration;
import com.soprasteria.g4it.backend.exception.UnableToGenerateFileException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
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
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
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
    private static final String CSV_SEPARATOR = ";";

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
    @Autowired
    private LoadInputFilesService loadInputFilesService;
    @Autowired
    private CsvFileMapperInfo csvFileMapperInfo;
    @Autowired
    private TaskRepository taskRepository;

    /**
     * Local working folder.
     */
    @Value("${batch.local.working.folder.base.path:}")
    private String localWorkingFolderBasePath;
    @Value("${local.working.folder}")
    private String localWorkingFolder;

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

        List<MultipartFile> cloudVirtualEquipmentFiles = new ArrayList<>();
        List<MultipartFile> cloudApplicationFiles = new ArrayList<>();

        for (final FileDescription file : session.getFiles()) {
            String errorMessage = null;

            if (file.getType() == FileType.EQUIPEMENT_VIRTUEL || file.getType() == FileType.APPLICATION) {
                if (file.getType() == FileType.EQUIPEMENT_VIRTUEL) {
                    try {
                        if (processVirtualEquipmentFile(file, storage, session, cloudVirtualEquipmentFiles)) {
                            errorMessage = "Error processing virtual equipment file";
                        }
                    } catch (IOException e) {
                        throw new InventoryLoadingException("Error processing virtual equipment file", e);
                    }
                } else {
                    try {
                        if (processApplicationFile(file, storage, session, cloudApplicationFiles)) {
                            errorMessage = "Error processing application file";
                        }
                    } catch (IOException e) {
                        throw new InventoryLoadingException("Error processing application file", e);
                    }
                }
            } else {
                errorMessage = "An error occurred while preparing working folder";
            }

            if (errorMessage != null) {
                try {
                    storage.moveAndRename(FileFolder.INPUT, FileFolder.WORK, file.getName(), filePath(session, file));
                } catch (IOException e) {
                    throw new InventoryLoadingException(errorMessage, e);
                }
            }
        }

        // Process all cloud files
        if (!cloudVirtualEquipmentFiles.isEmpty() || !cloudApplicationFiles.isEmpty()) {
            try {
                Task loadingTask = loadInputFilesService.loadFiles(
                        session.getSubscriber(),
                        session.getOrganizationId(),
                        session.getInventoryId(),
                        null,
                        null,
                        cloudVirtualEquipmentFiles.isEmpty() ? null : cloudVirtualEquipmentFiles,
                        cloudApplicationFiles.isEmpty() ? null : cloudApplicationFiles
                );

                // Wait until task is complete
                while (!TaskStatus.COMPLETED.toString().equals(loadingTask.getStatus())
                        && !TaskStatus.COMPLETED_WITH_ERRORS.toString().equals(loadingTask.getStatus())
                        && !TaskStatus.FAILED.toString().equals(loadingTask.getStatus())) {
                    Thread.sleep(2000);
                    loadingTask = taskRepository.findById(loadingTask.getId()).orElseThrow();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new InventoryLoadingException("Cloud file processing interrupted", e);
            }
        }
    }

    private boolean processVirtualEquipmentFile(FileDescription file, FileStorage storage,
                                                final InventoryLoadingSession session,
                                                List<MultipartFile> cloudVirtualEquipmentFiles) throws IOException {
        String fileName = file.getName();
        boolean isOldVirtual = false;
        List<Header> headers = csvFileMapperInfo.getMapping(FileType.INVENTORY_VIRTUAL_EQUIPMENT_CLOUD);
        String baseWorkPath = session.getSessionPath();
        Path tempDir = null;

        CSVFormat csvFormat = CSVFormat.RFC4180.builder()
                .setHeader()
                .setDelimiter(CSV_SEPARATOR)
                .setAllowMissingColumnNames(true)
                .setSkipHeaderRecord(false)
                .build();

        try (CSVParser records = csvFormat.parse(new InputStreamReader(storage.readFile(FileFolder.INPUT, fileName)))) {
            if (records.getHeaderNames().isEmpty()) {
                log.info("Empty input file {}, skipping processing", fileName);
                return false;
            }
            // Check if typeInfrastructure column exists
            if (!records.getHeaderNames().contains("typeInfrastructure")) {
                isOldVirtual = true;
                return isOldVirtual;
            }

            // Create temp directory only if we need to process the file
            tempDir = Path.of(localWorkingFolder, "work", baseWorkPath);
            Files.createDirectories(tempDir);
            Path oldFile = tempDir.resolve("old_" + fileName);
            Path cloudFile = tempDir.resolve("cloud_" + fileName);
            List<String> oldFileHeaders = headers.stream()
                    .limit(11)
                    .map(Header::getName)
                    .toList();

            int oldRecord = 0;
            int cloudRecord = 0;
            try (CSVPrinter oldWriter = new CSVPrinter(Files.newBufferedWriter(oldFile),
                    CSVFormat.Builder.create()
                            .setHeader(oldFileHeaders.toArray(String[]::new))
                            .setDelimiter(CsvUtils.DELIMITER)
                            .build());
                 CSVPrinter cloudWriter = new CSVPrinter(Files.newBufferedWriter(cloudFile),
                         CSVFormat.Builder.create()
                                 .setHeader(records.getHeaderNames().toArray(String[]::new))
                                 .setDelimiter(CsvUtils.DELIMITER)
                                 .build())) {

                for (CSVRecord csvRecord : records) {
                    String infrastructureType = csvRecord.get("typeInfrastructure").trim();

                    if (InfrastructureType.CLOUD_SERVICES.name().equals(infrastructureType)) {
                        cloudWriter.printRecord(csvRecord);
                        cloudRecord++;
                    } else if (infrastructureType.isEmpty() || InfrastructureType.NON_CLOUD_SERVERS.name().equals(infrastructureType)) {
                        oldWriter.printRecord(oldFileHeaders.stream().map(csvRecord::get).toList());
                        oldRecord++;
                    }
                }
            } catch (IOException e) {
                throw new InventoryLoadingException("Cannot write to old and write files", e);
            }

            if (cloudRecord + oldRecord == 0) {
                log.info("No valid records found in file {}", fileName);
                return true;
            }

            if (oldRecord > 0) {
                storage.upload(oldFile.toAbsolutePath().toString(), FileFolder.WORK,
                        baseWorkPath + "/" + file.getType().name() + "/old_" + fileName);
            }
            if (cloudFile.toFile().length() > 0) {
                try (InputStream cloudInputStream = new FileInputStream(cloudFile.toFile())) {
                    MultipartFile cloudMultipartFile = new InputStreamMultipartFile(
                            cloudInputStream,
                            file.getType().name(),
                            "cloud_" + fileName,
                            "text/csv"
                    );
                    cloudVirtualEquipmentFiles.add(cloudMultipartFile);
                }
            }
        } finally {
            cleanupTempDirectory(tempDir);
        }
        // delete original file
        storage.delete(FileFolder.INPUT, fileName);
        return isOldVirtual;
    }

    private boolean processApplicationFile(FileDescription file, FileStorage storage,
                                           final InventoryLoadingSession session,
                                           List<MultipartFile> cloudApplicationFiles) throws IOException {
        boolean isOldApplication = false;
        String fileName = file.getName();
        List<String> headers = csvFileMapperInfo.getMapping(FileType.APPLICATION).stream().map(Header::getName).toList();

        String baseWorkPath = session.getSessionPath();
        Path tempDir = null;

        CSVFormat csvFormat = CSVFormat.RFC4180.builder()
                .setHeader()
                .setDelimiter(CSV_SEPARATOR)
                .setAllowMissingColumnNames(true)
                .setSkipHeaderRecord(false)
                .build();

        try (CSVParser records = csvFormat.parse(new InputStreamReader(storage.readFile(FileFolder.INPUT, fileName)))) {

            if (records.getHeaderNames().isEmpty()) {
                log.info("Empty input file {}, skipping processing", fileName);
                return false;
            }

            // Check if isCloudApplication column exists
            if (!records.getHeaderNames().contains("associeAvecUnEquipmentCloud")) {
                return true;
            }

            // Create temp directory only if we need to process the file
            tempDir = Path.of(localWorkingFolder, "work", baseWorkPath);
            Files.createDirectories(tempDir);
            Path oldFile = tempDir.resolve("old_" + fileName);
            Path cloudFile = tempDir.resolve("cloud_" + fileName);

            int oldRecord = 0;
            int cloudRecord = 0;
            try (CSVPrinter oldWriter = new CSVPrinter(Files.newBufferedWriter(oldFile),
                    CSVFormat.Builder.create()
                            .setHeader(headers.toArray(String[]::new))
                            .setDelimiter(CsvUtils.DELIMITER)
                            .build());
                 CSVPrinter cloudWriter = new CSVPrinter(Files.newBufferedWriter(cloudFile),
                         CSVFormat.Builder.create()
                                 .setHeader(headers.toArray(String[]::new))
                                 .setDelimiter(CsvUtils.DELIMITER)
                                 .build())) {

                for (CSVRecord csvRecord : records) {

                    String isCloudApplication = csvRecord.get("associeAvecUnEquipmentCloud").trim();

                    if ("VRAI".equalsIgnoreCase(isCloudApplication)) {
                        cloudWriter.printRecord(headers.stream().map(csvRecord::get).toList());
                        cloudRecord++;
                    } else if ("FAUX".equalsIgnoreCase(isCloudApplication)) {
                        oldWriter.printRecord(headers.stream().map(csvRecord::get).toList());
                        oldRecord++;
                    }
                }
            } catch (IOException e) {
                throw new InventoryLoadingException("Cannot write to old and write files", e);
            }

            if (cloudRecord + oldRecord == 0) {
                log.info("No valid records found in file {}", fileName);
                return true;
            }

            if (oldRecord > 0) {
                storage.upload(oldFile.toAbsolutePath().toString(), FileFolder.WORK,
                        baseWorkPath + "/" + file.getType().name() + "/old_" + fileName);
            }

            if (cloudFile.toFile().length() > 0) {
                try (InputStream cloudInputStream = new FileInputStream(cloudFile.toFile())) {
                    MultipartFile cloudMultipartFile = new InputStreamMultipartFile(
                            cloudInputStream,
                            file.getType().name(),
                            "cloud_" + fileName,
                            "text/csv"
                    );
                    cloudApplicationFiles.add(cloudMultipartFile);
                }
            }

        } finally {
            cleanupTempDirectory(tempDir);
        }
        // delete original file
        storage.delete(FileFolder.INPUT, fileName);
        return isOldApplication;
    }

    private void cleanupTempDirectory(Path tempDir) {
        if (tempDir == null || !Files.exists(tempDir)) return;
        try {
            if (!Files.exists(tempDir)) return;

            boolean isDeleted = FileSystemUtils.deleteRecursively(tempDir);
            if (!isDeleted) {
                log.error("Unable to delete temp csv folder {}", tempDir.toFile().getAbsolutePath());
                throw new UnableToGenerateFileException();
            }
        } catch (IOException e) {
            log.error("Failed to cleanup temporary directory: {}", tempDir, e);
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
