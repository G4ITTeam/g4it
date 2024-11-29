/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice;

import com.soprasteria.g4it.backend.apifiles.business.FileSystemService;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.apiinventory.repository.InventoryRepository;
import com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.loadobject.LoadApplicationService;
import com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.loadobject.LoadDatacenterService;
import com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.loadobject.LoadPhysicalEquipmentService;
import com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.loadobject.LoadVirtualEquipmentService;
import com.soprasteria.g4it.backend.apiloadinputfiles.mapper.CsvToInMapper;
import com.soprasteria.g4it.backend.common.filesystem.model.CsvFileMapperInfo;
import com.soprasteria.g4it.backend.common.filesystem.model.FileFolder;
import com.soprasteria.g4it.backend.common.filesystem.model.FileType;
import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.model.LineError;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.exception.AsyncTaskException;
import com.soprasteria.g4it.backend.server.gen.api.dto.InApplicationRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.InDatacenterRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.InPhysicalEquipmentRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.InVirtualEquipmentRest;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static java.util.stream.Collectors.*;

@Service
@Slf4j
public class LoadFileService {

    @Value("${local.working.folder}")
    private String localWorkingFolder;

    @Autowired
    FileSystemService fileSystemService;

    @Autowired
    CsvFileMapperInfo csvFileMapperInfo;

    @Autowired
    MessageSource messageSource;

    @Autowired
    LoadDatacenterService loadDatacenterService;

    @Autowired
    LoadPhysicalEquipmentService loadPhysicalEquipmentService;

    @Autowired
    LoadVirtualEquipmentService loadVirtualEquipmentService;

    @Autowired
    LoadApplicationService loadApplicationService;

    @Autowired
    CsvToInMapper csvToInMapper;

    @Autowired
    InventoryRepository inventoryRepository;

    private static final String CSV_SEPARATOR = ";";
    private static final String CSV_EXT = ".csv";
    private static final String REJECTED = "rejected";

    @PostConstruct
    public void initFolder() throws IOException {
        Files.createDirectories(Path.of(localWorkingFolder, REJECTED));
    }

    /**
     * Manage a datacenter file
     *
     * @param context  the context
     * @param fileType the file type
     * @param filename the filename
     * @return the list of errors
     */
    public List<String> manageFile(final Context context, final FileType fileType, final String filename) {

        final String originalFileName = getOriginalFilename(fileType, filename);

        // download file
        Path filePath = downloadFile(context, filename, originalFileName);

        List<String> errors = new ArrayList<>();
        List<LineError> readErrors;

        try (Reader reader = new FileReader(filePath.toFile())) {

            CSVParser records = CSVFormat.RFC4180.builder()
                    .setHeader()
                    .setDelimiter(CSV_SEPARATOR)
                    .setAllowMissingColumnNames(true)
                    .setSkipHeaderRecord(false)
                    .build().parse(reader);

            Set<String> mandatoryHeaderFields = csvFileMapperInfo.getHeaderFields(fileType, true);
            records.getHeaderNames().forEach(mandatoryHeaderFields::remove);

            if (!mandatoryHeaderFields.isEmpty()) {
                errors.add(messageSource.getMessage(
                        "header.mandatory",
                        new String[]{originalFileName, String.join(", ", mandatoryHeaderFields)},
                        context.getLocale())
                );
                return errors;
            }

            Set<String> fileHeader = new HashSet<>(records.getHeaderNames());
            fileHeader.remove("");
            csvFileMapperInfo.getHeaderFields(fileType, false).forEach(fileHeader::remove);
            if (!fileHeader.isEmpty()) {
                errors.add(messageSource.getMessage(
                        "header.unknown",
                        new String[]{originalFileName, String.join(", ", fileHeader)},
                        context.getLocale()));
            }

            readErrors = switch (fileType) {
                case FileType.DATACENTER -> readDatacenters(context, records);
                case FileType.EQUIPEMENT_PHYSIQUE -> readPhysicalEquipments(context, records);
                case FileType.EQUIPEMENT_VIRTUEL -> readVirtualEquipments(context, records);
                case FileType.APPLICATION -> readApplications(context, records);
                default -> throw new IllegalArgumentException();
            };

        } catch (IOException e) {
            throw new AsyncTaskException(String.format("%s - Cannot read local csv file %s", context.log(), originalFileName), e);
        }

        if (!readErrors.isEmpty()) {
            writeRejected(context, readErrors, fileType, filePath, originalFileName);
        }

        return errors;
    }

    /**
     * Get original filename
     *
     * @param fileType the file type
     * @param filename the filename
     * @return the original filename
     */
    public String getOriginalFilename(final FileType fileType, final String filename) {
        return filename.substring(fileType.toString().length() + 1, filename.length() - 5 - UUID.randomUUID().toString().length());
    }

    /**
     * Download a file from file storage and put it in storagetmp/input/inventory
     *
     * @param context          the context
     * @param filename         the filename
     * @param originalFileName the original file name
     * @return the file path
     */
    private Path downloadFile(final Context context, final String filename, final String originalFileName) {

        Path filePath;
        try (InputStream is = fileSystemService.downloadFile(context.getSubscriber(), context.getOrganizationId(), FileFolder.INPUT, filename)) {

            filePath = Path.of(localWorkingFolder).resolve("input/inventory").resolve(filename);
            // copy file to local storage tmp
            FileUtils.copyInputStreamToFile(is, filePath.toFile());
        } catch (IOException e) {
            throw new AsyncTaskException(String.format("%s - Cannot download file %s from storage", context.log(), originalFileName), e);
        }
        return filePath;
    }

    /**
     * Append file rejected_${fileType.getFileName()}_local_date-time.csv
     *
     * @param context          the context
     * @param readErrors       the read errors
     * @param fileType         the file type
     * @param filePath         the filePath
     * @param originalFileName the original file name
     */
    private void writeRejected(final Context context, final List<LineError> readErrors, final FileType fileType, final Path filePath, final String originalFileName) {
        Map<Integer, List<String>> errorsByLine = readErrors.stream()
                .collect(groupingBy(LineError::line, mapping(LineError::error, toList())));

        String rejectedFileName = String.join("_", REJECTED, fileType.getFileName(), context.getDatetime().format(Constants.FILE_DATE_TIME_FORMATTER)) + CSV_EXT;

        final Path path = Path.of(localWorkingFolder).resolve(REJECTED).resolve(String.valueOf(context.getInventoryId())).resolve(rejectedFileName);
        try {
            Files.createDirectories(Path.of(localWorkingFolder).resolve(REJECTED).resolve(String.valueOf(context.getInventoryId())));
        } catch (IOException e) {
            throw new AsyncTaskException(String.format("%s - Cannot create local rejected folder", context.log()), e);
        }

        try (Reader reader = new FileReader(filePath.toFile());
             BufferedWriter writer = new BufferedWriter(new FileWriter(path.toFile(), true))
        ) {

            int lineNumber = 2;
            BufferedReader br = new BufferedReader(reader);
            String line = br.readLine();
            if (line != null) {
                // add to header
                writer.write(String.join(CSV_SEPARATOR, line, "inputFileName", "lineNumber", "message"));
                writer.newLine();
            }

            while (line != null) {
                line = br.readLine();
                List<String> errorLines = errorsByLine.get(lineNumber);
                if (errorLines != null) {
                    writer.write(String.join(CSV_SEPARATOR, line, originalFileName, String.valueOf(lineNumber), String.join(", ", errorLines)));
                    writer.newLine();
                }
                lineNumber++;
            }
        } catch (IOException e) {
            throw new AsyncTaskException(String.format("%s - Cannot read local csv file %s", context.log(), originalFileName), e);
        }
    }

    /**
     * Read datacenters from records
     *
     * @param context the context
     * @param records the CSVParser records
     * @return the list of error
     */
    private List<LineError> readDatacenters(final Context context, final CSVParser records) {
        int row = 1;
        int pageNumber = 0;
        List<LineError> errors = new ArrayList<>();

        // read file locally by PAGE_SIZE lines page
        List<InDatacenterRest> objects = new ArrayList<>(Constants.BATCH_SIZE);

        for (CSVRecord csvRecord : records) {
            objects.add(csvToInMapper.csvInDatacenterToRest(csvRecord, context.getInventoryId()));
            if (row >= Constants.BATCH_SIZE) {
                errors.addAll(loadDatacenterService.execute(context, pageNumber, objects));
                objects.clear();
                row = 1;
                pageNumber++;
            } else {
                row++;
            }
        }

        errors.addAll(loadDatacenterService.execute(context, pageNumber, objects));
        objects.clear();

        return errors;
    }

    /**
     * Read physical equipments from records
     *
     * @param context the context
     * @param records the CSVParser records
     * @return the list of error
     */
    private List<LineError> readPhysicalEquipments(final Context context, final CSVParser records) {
        int row = 1;
        int pageNumber = 0;
        List<LineError> errors = new ArrayList<>();

        // read file locally by PAGE_SIZE lines page
        List<InPhysicalEquipmentRest> objects = new ArrayList<>(Constants.BATCH_SIZE);

        for (CSVRecord csvRecord : records) {
            objects.add(csvToInMapper.csvInPhysicalEquipmentToRest(csvRecord, context.getInventoryId()));
            if (row >= Constants.BATCH_SIZE) {
                errors.addAll(loadPhysicalEquipmentService.execute(context, pageNumber, objects));
                objects.clear();
                row = 1;
                pageNumber++;
            } else {
                row++;
            }
        }

        errors.addAll(loadPhysicalEquipmentService.execute(context, pageNumber, objects));
        objects.clear();

        return errors;
    }

    /**
     * Read virtual equipments from records
     *
     * @param context the context
     * @param records the CSVParser records
     * @return the list of error
     */
    private List<LineError> readVirtualEquipments(final Context context, final CSVParser records) {
        int row = 1;
        int pageNumber = 0;
        List<LineError> errors = new ArrayList<>();

        // read file locally by PAGE_SIZE lines page
        List<InVirtualEquipmentRest> objects = new ArrayList<>(Constants.BATCH_SIZE);

        for (CSVRecord csvRecord : records) {
            objects.add(csvToInMapper.csvInVirtualEquipmentToRest(csvRecord, context.getInventoryId()));
            if (row >= Constants.BATCH_SIZE) {
                errors.addAll(loadVirtualEquipmentService.execute(context, pageNumber, objects));
                objects.clear();
                row = 1;
                pageNumber++;
            } else {
                row++;
            }
        }

        errors.addAll(loadVirtualEquipmentService.execute(context, pageNumber, objects));
        objects.clear();

        return errors;
    }

    /**
     * Read applications from records
     *
     * @param context the context
     * @param records the CSVParser records
     * @return the list of error
     */
    private List<LineError> readApplications(final Context context, final CSVParser records) {
        int row = 1;
        int pageNumber = 0;
        List<LineError> errors = new ArrayList<>();

        // read file locally by PAGE_SIZE lines page
        List<InApplicationRest> objects = new ArrayList<>(Constants.BATCH_SIZE);

        for (CSVRecord csvRecord : records) {
            objects.add(csvToInMapper.csvInApplicationToRest(csvRecord, context.getInventoryId()));
            if (row >= Constants.BATCH_SIZE) {
                errors.addAll(loadApplicationService.execute(context, pageNumber, objects));
                objects.clear();
                row = 1;
                pageNumber++;
            } else {
                row++;
            }
        }

        errors.addAll(loadApplicationService.execute(context, pageNumber, objects));
        objects.clear();

        return errors;
    }

    @Transactional
    void setInventoryCounts(final Long inventoryId) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow();

        inventory.setDataCenterCount(loadDatacenterService.getDatacenterCount(inventoryId));
        inventory.setPhysicalEquipmentCount(loadPhysicalEquipmentService.getPhysicalEquipmentCount(inventoryId));
        inventory.setVirtualEquipmentCount(loadVirtualEquipmentService.getVirtualEquipmentCount(inventoryId));
        inventory.setApplicationCount(loadApplicationService.getApplicationCount(inventoryId));

        inventoryRepository.save(inventory);
    }

}
