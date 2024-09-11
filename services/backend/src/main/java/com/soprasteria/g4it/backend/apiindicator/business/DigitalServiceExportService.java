/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiindicator.business;

import com.soprasteria.g4it.backend.apidigitalservice.modeldb.*;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceRepository;
import com.soprasteria.g4it.backend.apiindicator.modeldb.numecoeval.PhysicalEquipmentIndicator;
import com.soprasteria.g4it.backend.apiindicator.repository.numecoeval.PhysicalEquipmentIndicatorRepository;
import com.soprasteria.g4it.backend.common.filesystem.business.local.LocalFileService;
import com.soprasteria.g4it.backend.common.filesystem.model.FileMapperInfo;
import com.soprasteria.g4it.backend.common.filesystem.model.FileType;
import com.soprasteria.g4it.backend.common.filesystem.model.Header;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.soprasteria.g4it.backend.common.utils.ObjectUtils.getCsvString;

@Slf4j
@Service
public class DigitalServiceExportService {

    private static final String LOG_FILE_CREATED = "Digital-service Export - '{}' {} file created successfully";
    private static final String LOG_NO_DATA = "Digital-service Export - '{}' {} file not created, no data";
    private static final String DELIMITER = ";";
    @Autowired
    private DigitalServiceRepository digitalServiceRepository;
    @Value("${local.working.folder}")
    private String localWorkingFolder;
    @Autowired
    private FileMapperInfo csvFileMapperInfo;
    @Autowired
    private PhysicalEquipmentIndicatorRepository physicalEquipmentIndicatorRepository;
    @Autowired
    private LocalFileService localFileService;

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
        final Path digitalServicePath = Path.of(localWorkingFolder, "export", "digital-service", digitalServiceUid);
        final Path directoryPath = digitalServicePath.resolve("csvFiles");

        if (digitalServicePath.toFile().exists()) {
            FileSystemUtils.deleteRecursively(directoryPath);
        }
        Files.createDirectories(directoryPath);

        final DigitalService digitalService = digitalServiceRepository.findById(digitalServiceUid)
                .orElseThrow(() -> new G4itRestException("404", "Digital service not found"));

        createTerminalFile(directoryPath, digitalService);
        createNetworkFile(directoryPath, digitalService);
        createServerFile(directoryPath, digitalService);
        createPhysicalEquipmentIndicatorFile(directoryPath, digitalService);
        createDatacenterFile(directoryPath, digitalService);
        createVirtualMachineFile(directoryPath, digitalService);

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

        servers.forEach(server -> {
            server.getVirtualEquipmentDigitalServices().forEach(vmDigitalService ->
                    dataLines.add(String.join(DELIMITER,
                            digitalService.getName(),
                            fieldNamesWithDsName.stream().map(field -> getCsvString(field, vmDigitalService, VirtualEquipmentDigitalService.class)).collect(Collectors.joining(DELIMITER)))
                    )
            );
        });

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
                                                      final DigitalService digitalService) {
        final String type = "ind_physical_equipment";
        final String digitalServiceUid = digitalService.getUid();

        final Page<PhysicalEquipmentIndicator> physicalEquipmentIndicators = physicalEquipmentIndicatorRepository.findByBatchName(digitalServiceUid, Pageable.unpaged());

        if (physicalEquipmentIndicators.isEmpty()) {
            log.info(LOG_NO_DATA, digitalServiceUid, type);
            return;
        }

        final List<String> dataLines = new ArrayList<>();
        final List<Header> headers = csvFileMapperInfo.getMapping(FileType.PHYSICAL_EQUIPMENT_INDICATOR_DIGITAL_SERVICE);
        final List<String> fieldNamesWithDsName = getFieldList(headers);

        dataLines.add(headers.stream().map(Header::getName).collect(Collectors.joining(DELIMITER)));

        physicalEquipmentIndicators.forEach(item ->
                dataLines.add(String.join(DELIMITER,
                        digitalService.getName(),
                        fieldNamesWithDsName.stream().map(field -> getCsvString(field, item, PhysicalEquipmentIndicator.class)).collect(Collectors.joining(DELIMITER)))
                )
        );

        localFileService.writeFile(directoryPath.resolve(type + ".csv"), dataLines);
        log.info(LOG_FILE_CREATED, digitalServiceUid, type);
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
