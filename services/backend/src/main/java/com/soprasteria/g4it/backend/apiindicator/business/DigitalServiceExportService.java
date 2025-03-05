/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiindicator.business;

import com.azure.storage.blob.models.BlobErrorCode;
import com.azure.storage.blob.models.BlobStorageException;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.*;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceRepository;
import com.soprasteria.g4it.backend.apifiles.business.FileSystemService;
import com.soprasteria.g4it.backend.apiindicator.modeldb.numecoeval.PhysicalEquipmentIndicator;
import com.soprasteria.g4it.backend.apiindicator.repository.numecoeval.PhysicalEquipmentIndicatorRepository;
import com.soprasteria.g4it.backend.apiindicator.utils.CriteriaUtils;
import com.soprasteria.g4it.backend.apiinout.modeldb.InVirtualEquipment;
import com.soprasteria.g4it.backend.apiinout.repository.InVirtualEquipmentRepository;
import com.soprasteria.g4it.backend.apireferential.business.ReferentialService;
import com.soprasteria.g4it.backend.common.criteria.CriteriaService;
import com.soprasteria.g4it.backend.common.filesystem.business.local.LocalFileService;
import com.soprasteria.g4it.backend.common.filesystem.model.FileFolder;
import com.soprasteria.g4it.backend.common.filesystem.model.FileMapperInfo;
import com.soprasteria.g4it.backend.common.filesystem.model.FileType;
import com.soprasteria.g4it.backend.common.filesystem.model.Header;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.common.utils.StringUtils;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.soprasteria.g4it.backend.common.utils.ObjectUtils.getCsvString;

@Slf4j
@Service
public class DigitalServiceExportService {

    private static final String LOG_FILE_CREATED = "Digital-service Export - '{}' {} file created successfully";
    private static final String LOG_NO_DATA = "Digital-service Export - '{}' {} file not created, no data";
    private static final String DELIMITER = ";";
    private static final String IND_CLOUD_INSTANCES = "ind_cloud_instances";
    @Autowired
    private DigitalServiceRepository digitalServiceRepository;
    @Value("${local.working.folder}")
    private String localWorkingFolder;
    @Autowired
    private FileMapperInfo csvFileMapperInfo;
    @Autowired
    private PhysicalEquipmentIndicatorRepository physicalEquipmentIndicatorRepository;
    @Autowired
    private InVirtualEquipmentRepository inVirtualEquipmentRepository;
    @Autowired
    private LocalFileService localFileService;
    @Autowired
    private FileSystemService fileSystemService;
    @Autowired
    private ReferentialService referentialService;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private CriteriaService criteriaService;

    @PostConstruct
    public void initFolder() throws IOException {
        Files.createDirectories(Path.of(localWorkingFolder, "export", "digital-service"));
    }

    /**
     * Create all csv files
     *
     * @param digitalServiceUid digital service uid
     * @param subscriber        subscriber name
     * @param organization      organization name
     * @return zip file containing all csv files
     * @throws IOException exception
     */
    public InputStream createFiles(final String digitalServiceUid, final String subscriber, final Long organization) throws IOException {

        final DigitalService digitalService = digitalServiceRepository.findById(digitalServiceUid)
                .orElseThrow(() -> new G4itRestException("404", "Digital service not found"));

        if (Boolean.TRUE.equals(digitalService.getIsNewArch())) {
            Task task = taskRepository.findByDigitalServiceUid(digitalServiceUid)
                    .orElseThrow(() -> new G4itRestException("404", "Digital service task not found"));
            String filename = task.getId() + Constants.ZIP;
            return fileSystemService.downloadFile(subscriber, organization, FileFolder.EXPORT, filename);
        }

        final Path digitalServicePath = Path.of(localWorkingFolder, "export", "digital-service", digitalServiceUid);
        final Path directoryPath = digitalServicePath.resolve("csvFiles");

        if (digitalServicePath.toFile().exists()) {
            FileSystemUtils.deleteRecursively(directoryPath);
        }
        Files.createDirectories(directoryPath);

        List<String> criteriaKeyList = criteriaService.getSelectedCriteriaForDigitalService(subscriber, organization, digitalService.getCriteria()).active();

        createTerminalFile(directoryPath, digitalService);
        createNetworkFile(directoryPath, digitalService);
        createServerFile(directoryPath, digitalService);
        createPhysicalEquipmentIndicatorFile(directoryPath, digitalService, criteriaKeyList);
        createDatacenterFile(directoryPath, digitalService);
        createVirtualMachineFile(directoryPath, digitalService);
        int cloudInstanceFileSize = createCloudInstanceFile(directoryPath, digitalService);
        if (cloudInstanceFileSize > 0) {
            createCloudInstanceIndicatorFile(subscriber, organization, directoryPath, digitalService.getUid());
        }
        File zipFile = localFileService.createZipFile(
                directoryPath,
                digitalServicePath.resolve(String.join("_", "g4it", subscriber, String.valueOf(organization), digitalServiceUid, "export-result-files.zip"))
        );

        FileSystemUtils.deleteRecursively(directoryPath);
        return new FileInputStream(zipFile);
    }

    /**
     * Create datacenter file
     *
     * @param directoryPath  directory path
     * @param digitalService digital service
     */
    private void createDatacenterFile(final Path directoryPath, final DigitalService digitalService) {
        final String type = "datacenter";
        final String digitalServiceUid = digitalService.getUid();

        List<DatacenterDigitalService> datacenters = digitalService.getDatacenterDigitalServices();

        if (datacenters.isEmpty()) {
            log.info(LOG_NO_DATA, digitalService.getUid(), type);
            return;
        }

        final List<String> dataLines = new ArrayList<>();
        final List<Header> headers = csvFileMapperInfo.getMapping(FileType.DATACENTER_DIGITAL_SERVICE);
        final List<String> fieldNamesWithDsName = getFieldList(headers);

        dataLines.add(headers.stream().map(Header::getName).collect(Collectors.joining(DELIMITER)));

        datacenters.forEach(item ->
                dataLines.add(String.join(DELIMITER,
                        digitalService.getName(),
                        fieldNamesWithDsName.stream().map(field -> getCsvString(field, item, DatacenterDigitalService.class)).collect(Collectors.joining(DELIMITER)))
                )
        );

        localFileService.writeFile(directoryPath.resolve(type + ".csv"), dataLines);
        log.info(LOG_FILE_CREATED, digitalServiceUid, type);
    }

    /**
     * Create terminal file
     *
     * @param directoryPath  directory path
     * @param digitalService digital service
     */
    private void createTerminalFile(final Path directoryPath, final DigitalService digitalService) {

        final String type = "terminal";
        final String digitalServiceUid = digitalService.getUid();

        List<Terminal> terminals = digitalService.getTerminals();
        if (terminals.isEmpty()) {
            log.info(LOG_NO_DATA, digitalServiceUid, type);
            return;
        }

        final List<String> dataLines = new ArrayList<>();
        final List<Header> headers = csvFileMapperInfo.getMapping(FileType.TERMINAL);
        final List<String> fieldNamesWithDsName = getFieldList(headers);

        dataLines.add(headers.stream().map(Header::getName).collect(Collectors.joining(DELIMITER)));

        terminals.forEach(item ->
                dataLines.add(String.join(DELIMITER,
                        digitalService.getName(),
                        fieldNamesWithDsName.stream().map(field -> getCsvString(field, item, Terminal.class)).collect(Collectors.joining(DELIMITER)))
                )
        );

        localFileService.writeFile(directoryPath.resolve(type + ".csv"), dataLines);
        log.info(LOG_FILE_CREATED, digitalServiceUid, type);
    }

    /**
     * Create network file
     *
     * @param directoryPath  directory path
     * @param digitalService digital service
     */
    private void createNetworkFile(final Path directoryPath, final DigitalService digitalService) {

        final String type = "network";
        final String digitalServiceUid = digitalService.getUid();

        List<Network> networks = digitalService.getNetworks();
        if (networks.isEmpty()) {
            log.info(LOG_NO_DATA, digitalServiceUid, type);
            return;
        }
        final List<String> dataLines = new ArrayList<>();
        final List<Header> headers = csvFileMapperInfo.getMapping(FileType.NETWORK);
        final List<String> fieldNamesWithDsName = getFieldList(headers);
        dataLines.add(headers.stream().map(Header::getName).collect(Collectors.joining(DELIMITER)));

        networks.forEach(item ->
                dataLines.add(String.join(DELIMITER,
                        digitalService.getName(),
                        fieldNamesWithDsName.stream().map(field -> getCsvString(field, item, Network.class)).collect(Collectors.joining(DELIMITER)))
                )
        );

        localFileService.writeFile(directoryPath.resolve(type + ".csv"), dataLines);
        log.info(LOG_FILE_CREATED, digitalServiceUid, type);
    }

    /**
     * Create servers file
     *
     * @param directoryPath  directory path
     * @param digitalService digital service
     */
    private void createServerFile(final Path directoryPath, final DigitalService digitalService) {

        final String type = "server";
        final String digitalServiceUid = digitalService.getUid();


        List<Server> servers = digitalService.getServers();
        if (servers.isEmpty()) {
            log.info(LOG_NO_DATA, digitalServiceUid, type);
            return;
        }

        final List<String> dataLines = new ArrayList<>();
        final List<Header> headers = csvFileMapperInfo.getMapping(FileType.SERVER);
        final List<String> fieldNamesWithDsName = getFieldList(headers);

        dataLines.add(headers.stream().map(Header::getName).collect(Collectors.joining(DELIMITER)));

        servers.forEach(server -> {
            StringBuilder csvLine = new StringBuilder();

            csvLine.append(digitalService.getName());

            fieldNamesWithDsName.forEach(field -> {
                if (!shouldSkipField(csvLine.toString(), field)) {
                    csvLine.append(DELIMITER).append(getCsvString(field, server, Server.class));
                } else {
                    csvLine.append(DELIMITER);
                }
            });
            dataLines.add(csvLine.toString());
        });


        localFileService.writeFile(directoryPath.resolve(type + ".csv"), dataLines);
        log.info(LOG_FILE_CREATED, digitalServiceUid, type);
    }

    /**
     * @param csvLine each csv line
     * @param field   field to set
     * @return value to set
     */
    private boolean shouldSkipField(String csvLine, String field) {
        return csvLine.contains("COMPUTE") && "serverHostTotalDisk".equals(field) || csvLine.contains("STORAGE") && "serverHostNbOfVcpu".equals(field);
    }

    /**
     * Create virtual machines file
     *
     * @param directoryPath  directory path
     * @param digitalService digital service
     */
    private void createVirtualMachineFile(final Path directoryPath, final DigitalService digitalService) {
        final String type = "virtual_machines";
        final String digitalServiceUid = digitalService.getUid();

        List<Server> servers = digitalService.getServers();
        if (servers.isEmpty()) {
            log.info(LOG_NO_DATA, digitalServiceUid, type);
            return;
        }

        final List<String> dataLines = new ArrayList<>();
        final List<Header> headers = csvFileMapperInfo.getMapping(FileType.VIRTUAL_MACHINE);
        final List<String> fieldNamesWithDsName = getFieldList(headers);

        dataLines.add(headers.stream().map(Header::getName).collect(Collectors.joining(DELIMITER)));

        servers.forEach(server -> server.getVirtualEquipmentDigitalServices().forEach(vmDigitalService ->
                dataLines.add(String.join(DELIMITER,
                        digitalService.getName(),
                        fieldNamesWithDsName.stream().map(field -> getCsvString(field, vmDigitalService, VirtualEquipmentDigitalService.class)).collect(Collectors.joining(DELIMITER)))
                )
        ));

        localFileService.writeFile(directoryPath.resolve(type + ".csv"), dataLines);
        log.info(LOG_FILE_CREATED, digitalServiceUid, type);
    }

    /**
     * Create physical equipment indicator file
     *
     * @param directoryPath  directory path
     * @param digitalService digital service
     */
    private void createPhysicalEquipmentIndicatorFile(final Path directoryPath,
                                                      final DigitalService digitalService, final List<String> criteriaKeyList) {
        final String type = "ind_physical_equipment";
        final String digitalServiceUid = digitalService.getUid();

        final Page<PhysicalEquipmentIndicator> physicalEquipmentIndicators = physicalEquipmentIndicatorRepository.findByBatchName(digitalServiceUid, Pageable.unpaged());

        if (physicalEquipmentIndicators.isEmpty()) {
            log.info(LOG_NO_DATA, digitalServiceUid, type);
            return;
        }

        final List<String> dataLines = new ArrayList<>();
        final List<Header> headers = csvFileMapperInfo.getMapping(FileType.PHYSICAL_EQUIPMENT_INDICATOR_DIGITAL_SERVICE);

        // skip for fields digitalServiceName and sipImpact
        final List<String> fieldNamesWithDsName = headers.stream()
                .filter(field -> headers.indexOf(field) != 0 && headers.indexOf(field) != 24)
                .map(field -> field.getDbName() == null ? field.getName() : field.getDbName())
                .toList();

        dataLines.add(headers.stream().map(Header::getName).collect(Collectors.joining(DELIMITER)));

        List<String> activeCriteria = criteriaKeyList.stream().filter(CriteriaUtils.CRITERIA_MAP::containsKey)
                .map(StringUtils::kebabToSnakeCase)
                .toList();

        Map<String, Double> refSipByCriteria = referentialService.getSipValueMap(activeCriteria);

        physicalEquipmentIndicators.forEach(item ->
                dataLines.add(String.join(DELIMITER,
                        digitalService.getName(),
                        fieldNamesWithDsName.stream()
                                .map(field -> getCsvString(field, item, PhysicalEquipmentIndicator.class))
                                .collect(Collectors.joining(DELIMITER)),
                        String.valueOf(
                                (refSipByCriteria.get(item.getCommonCriteria()) == null || item.getUnitImpact() == null)
                                        ? 0d
                                        : item.getUnitImpact() / refSipByCriteria.get(item.getCommonCriteria())
                        )
                ))
        );

        localFileService.writeFile(directoryPath.resolve(type + ".csv"), dataLines);
        log.info(LOG_FILE_CREATED, digitalServiceUid, type);
    }

    /**
     * Create cloud instance file
     *
     * @param directoryPath  directory path
     * @param digitalService digital service
     */
    private int createCloudInstanceFile(final Path directoryPath, final DigitalService digitalService) {
        final String type = "cloud_instances";
        final String digitalServiceUid = digitalService.getUid();

        List<InVirtualEquipment> inVirtualEquipments = inVirtualEquipmentRepository.findByDigitalServiceUid(digitalServiceUid);

        if (inVirtualEquipments.isEmpty()) {
            log.info(LOG_NO_DATA, digitalServiceUid, type);
            return 0;
        }
        final List<String> dataLines = new ArrayList<>();
        final List<Header> headers = csvFileMapperInfo.getMapping(FileType.CLOUD_INSTANCE);
        final List<String> fieldNamesWithDsName = getFieldList(headers);

        dataLines.add(headers.stream().map(Header::getName).collect(Collectors.joining(DELIMITER)));

        inVirtualEquipments.forEach(item ->
                dataLines.add(String.join(DELIMITER,
                        digitalService.getName(),
                        fieldNamesWithDsName.stream().map(field -> getCsvString(field, item, InVirtualEquipment.class)).collect(Collectors.joining(DELIMITER)))
                )
        );
        localFileService.writeFile(directoryPath.resolve(type + ".csv"), dataLines);
        log.info(LOG_FILE_CREATED, digitalServiceUid, type);
        return inVirtualEquipments.size();
    }

    /**
     * Create cloud instance indicator file
     *
     * @param subscriber        the subscriber
     * @param organization      the organization
     * @param directoryPath     directory path
     * @param digitalServiceUid digital service uid
     */
    private void createCloudInstanceIndicatorFile(final String subscriber, final Long organization, final Path directoryPath, final String digitalServiceUid) {
        Optional<Task> task = taskRepository.findByDigitalServiceUid(digitalServiceUid);
        if (task.isEmpty()) {
            log.info(LOG_NO_DATA, digitalServiceUid, IND_CLOUD_INSTANCES);
            return;
        }
        final String fileName = task.get().getId() + ".zip";
        try {
            InputStream inputStream = fileSystemService.downloadFile(subscriber, organization, FileFolder.EXPORT, fileName);

            if (inputStream == null) {
                log.info(LOG_NO_DATA, digitalServiceUid, IND_CLOUD_INSTANCES);
                return;
            }

            processCloudInstanceIndicatorZip(inputStream, directoryPath, digitalServiceUid);

        } catch (FileNotFoundException e) {
            log.info(LOG_NO_DATA, digitalServiceUid, IND_CLOUD_INSTANCES);
        } catch (BlobStorageException e) {
            if (e.getErrorCode().equals(BlobErrorCode.BLOB_NOT_FOUND)) {
                log.info(LOG_NO_DATA, digitalServiceUid, IND_CLOUD_INSTANCES);
                return;
            }
            throw new G4itRestException("500", String.format("Something went wrong downloading file %s", fileName), e);
        } catch (IOException e) {
            throw new G4itRestException("500", String.format("Something went wrong processing file %s", fileName), e);
        }
    }

    /**
     * Process the cloud instance indicator zip file and extract its contents
     *
     * @param inputStream       Input stream of the zip file
     * @param directoryPath     Target directory path
     * @param digitalServiceUid Digital service UID
     * @throws IOException If there's an error processing the file
     */
    private void processCloudInstanceIndicatorZip(InputStream inputStream, Path directoryPath,
                                                  String digitalServiceUid) throws IOException {
        // temporary directory to extract the zip
        Path tempDir = Files.createTempDirectory("cloud-instance-indicator");

        try (inputStream) {
            localFileService.unzipFile(inputStream, tempDir);
            Path indicatorFile = tempDir.resolve(IND_CLOUD_INSTANCES + ".csv");

            if (Files.exists(indicatorFile)) {
                // Read and process the CSV file
                List<String> dataLines = Files.readAllLines(indicatorFile);

                if (!dataLines.isEmpty()) {
                    localFileService.writeFile(directoryPath.resolve(IND_CLOUD_INSTANCES + ".csv"), dataLines);
                    log.info(LOG_FILE_CREATED, digitalServiceUid, IND_CLOUD_INSTANCES);
                } else {
                    log.info(LOG_NO_DATA, digitalServiceUid, IND_CLOUD_INSTANCES);
                }
            } else {
                log.info(LOG_NO_DATA, digitalServiceUid, IND_CLOUD_INSTANCES);
            }

        } finally {
            // Clean up temporary directory
            FileSystemUtils.deleteRecursively(tempDir);
        }
    }

    /**
     * Get actual field list from list of headers
     * Skip the first element which is always digitalServiceName
     *
     * @param headers the list of headers
     * @return the list of header mapped
     */
    private List<String> getFieldList(List<Header> headers) {
        return headers.stream()
                .skip(1)
                .map(field -> field.getDbName() == null ? field.getName() : field.getDbName()).toList();
    }

}
