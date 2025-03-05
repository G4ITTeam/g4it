/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiindicator.controller;

import com.azure.storage.blob.models.BlobErrorCode;
import com.azure.storage.blob.models.BlobStorageException;
import com.soprasteria.g4it.backend.apibatchexport.business.InventoryExportService;
import com.soprasteria.g4it.backend.apifiles.business.FileSystemService;
import com.soprasteria.g4it.backend.apiindicator.business.InventoryIndicatorService;
import com.soprasteria.g4it.backend.apiindicator.mapper.IndicatorRestMapper;
import com.soprasteria.g4it.backend.apiindicator.model.ApplicationImpactBO;
import com.soprasteria.g4it.backend.apiindicator.model.ApplicationIndicatorBO;
import com.soprasteria.g4it.backend.apiindicator.model.EquipmentIndicatorBO;
import com.soprasteria.g4it.backend.apiinventory.business.InventoryService;
import com.soprasteria.g4it.backend.apiinventory.model.InventoryBO;
import com.soprasteria.g4it.backend.apiinventory.model.InventoryExportReportBO;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.common.filesystem.model.FileFolder;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.common.utils.ExportBatchStatus;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import com.soprasteria.g4it.backend.server.gen.api.InventoryIndicatorApiDelegate;
import com.soprasteria.g4it.backend.server.gen.api.dto.*;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Inventory Indicator Job Service.
 */
@Service
@NoArgsConstructor
public class InventoryIndicatorController implements InventoryIndicatorApiDelegate {

    private static final String ERROR_DOWNLOADING_FILE = "Something went wrong downloading file %s";
    private static final String FILE_NOT_FOUND = "file %s not found in filestorage";
    @Autowired
    private InventoryIndicatorService inventoryIndicatorService;
    @Autowired
    private InventoryService inventoryService;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private InventoryExportService inventoryExportService;
    @Autowired
    private IndicatorRestMapper indicatorRestMapper;
    @Autowired
    private FileSystemService fileSystemService;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Map<String, EquipmentIndicatorRest>> getEquipmentIndicators(final String subscriber,
                                                                                      final Long organization,
                                                                                      final Long inventoryId) {
        final Map<String, EquipmentIndicatorBO> indicators = inventoryIndicatorService.getEquipmentIndicators(subscriber, organization, inventoryId);
        return ResponseEntity.ok().body(indicators.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, map -> this.indicatorRestMapper.toDto(map.getValue()))));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<ApplicationIndicatorRest>> getApplicationIndicators(final String subscriber,
                                                                                   final Long organization,
                                                                                   final Long inventoryId) {
        final List<ApplicationIndicatorBO<ApplicationImpactBO>> indicators = inventoryIndicatorService.getApplicationIndicators(subscriber, organization, inventoryId);
        return ResponseEntity.ok().body(this.indicatorRestMapper.toApplicationIndicatorDto(indicators));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<PhysicalEquipmentLowImpactRest>> getPhysicalEquipmentsLowImpact(final String subscriber,
                                                                                               final Long organization,
                                                                                               final Long inventoryId) {
        return ResponseEntity.ok(indicatorRestMapper.toLowImpactDto(inventoryIndicatorService.getPhysicalEquipmentsLowImpact(subscriber, organization, inventoryId)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<PhysicalEquipmentsAvgAgeRest>> getPhysicalEquipmentAvgAge(final String subscriber,
                                                                                         final Long organization,
                                                                                         final Long inventoryId) {
        return ResponseEntity.ok(indicatorRestMapper.toAvgAgeDto(inventoryIndicatorService.getPhysicalEquipmentAvgAge(subscriber, organization, inventoryId)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<PhysicalEquipmentElecConsumptionRest>> getPhysicalEquipmentElecConsumption(final String subscriber,
                                                                                                          final Long organizationId,
                                                                                                          final Long inventoryId) {
        return ResponseEntity.ok(indicatorRestMapper.toElecConsumptionDto(inventoryIndicatorService.getPhysicalEquipmentElecConsumption(subscriber, organizationId, inventoryId)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> deleteIndicators(final String subscriber,
                                                 final Long organization,
                                                 final Long inventoryId) {
        inventoryIndicatorService.deleteIndicators(subscriber, organization, inventoryId);
        return ResponseEntity.noContent().build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<DataCentersInformationRest>> getDataCenterIndicators(final String subscriber,
                                                                                    final Long organization,
                                                                                    final Long inventoryId) {
        return ResponseEntity.ok().body(
                indicatorRestMapper.toDataCenterDto(inventoryIndicatorService.getDataCenterIndicators(subscriber, organization, inventoryId))
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> exportIndicators(final String subscriber,
                                                 final Long organization,
                                                 final Long inventoryId) {
        inventoryExportService.createExportRequest(subscriber, organization, inventoryId);
        return ResponseEntity.<Void>ok().build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Resource> getIndicatorsExportResult(final String subscriber,
                                                              final Long organization,
                                                              final Long inventoryId) {
        final InventoryBO inventory = inventoryService.getInventory(subscriber, organization, inventoryId);
        String filename = null;

        if (Boolean.TRUE.equals(inventory.getIsNewArch())) {
            Task task = taskRepository.findByInventoryAndLastCreationDate(Inventory.builder().id(inventoryId).build())
                    .orElseThrow(() -> new G4itRestException("404", String.format("task of inventoryId '%d' is not found", inventoryId)));
            filename = task.getId() + Constants.ZIP;
        } else {
            InventoryExportReportBO exportReport = inventoryExportService.getExportReportByInventoryId(subscriber, organization, inventoryId);

            if (exportReport == null || !exportReport.getBatchStatusCode().equals(ExportBatchStatus.EXPORT_GENERATED.name())) {
                throw new G4itRestException("404", String.format("export report is not found in inventory %s on %s/%s",
                        inventoryId, subscriber, organization));
            }
            String filenameOldArch = fileSystemService.getFilenameFromUrl(exportReport.getResultFileUrl(), 0);

            final String filePath = String.join("/", subscriber, organization.toString(), FileFolder.EXPORT.getFolderName(), filenameOldArch);
            Optional<Task> task = taskRepository.findByInventoryAndLastCreationDate(Inventory.builder().id(inventoryId).build());
            if (task.isEmpty()) {
                return downloadFile(subscriber, organization, filenameOldArch, filePath);
            }

            String cloudFilename = task.get().getId() + Constants.ZIP;
            try {
                InputStream inputStreamCloud = null;
                try {
                    inputStreamCloud = fileSystemService.downloadFile(subscriber, organization,
                            FileFolder.EXPORT, cloudFilename);
                } catch (Exception e) {
                    return downloadFile(subscriber, organization, filenameOldArch, filePath);
                }

                if (inputStreamCloud != null) {
                    ByteArrayOutputStream baosCloud = new ByteArrayOutputStream();
                    ByteArrayOutputStream baosIndCloud = new ByteArrayOutputStream();
                    ByteArrayOutputStream baosCloudApp = new ByteArrayOutputStream();
                    ByteArrayOutputStream baosIndCloudApp = new ByteArrayOutputStream();
                    boolean cloudFound = false;
                    boolean indCloudFound = false;
                    boolean cloudAppFound = false;
                    boolean indCloudAppFound = false;

                    try (ZipInputStream zis = new ZipInputStream(inputStreamCloud)) {
                        ZipEntry entry;
                        while ((entry = zis.getNextEntry()) != null) {
                            if (entry.getName().equals("ind_virtual_equipment.csv")) {
                                indCloudFound = true;
                                byte[] buffer = new byte[8192];
                                int len;
                                while ((len = zis.read(buffer)) > 0) {
                                    baosIndCloud.write(buffer, 0, len);
                                }
                                zis.closeEntry();
                            }
                            if (entry.getName().equals("virtual_equipment.csv")) {
                                cloudFound = true;
                                byte[] buffer = new byte[8192];
                                int len;
                                while ((len = zis.read(buffer)) > 0) {
                                    baosCloud.write(buffer, 0, len);
                                }
                                zis.closeEntry();
                            }
                            if (entry.getName().equals("ind_application.csv")) {
                                indCloudAppFound = true;
                                byte[] buffer = new byte[8192];
                                int len;
                                while ((len = zis.read(buffer)) > 0) {
                                    baosIndCloudApp.write(buffer, 0, len);
                                }
                                zis.closeEntry();
                            }
                            if (entry.getName().equals("application.csv")) {
                                cloudAppFound = true;
                                byte[] buffer = new byte[8192];
                                int len;
                                while ((len = zis.read(buffer)) > 0) {
                                    baosCloudApp.write(buffer, 0, len);
                                }
                                zis.closeEntry();
                            }
                        }
                    }

                    if (cloudFound || indCloudFound || cloudAppFound || indCloudAppFound) {
                        return processZipContent(subscriber, organization, filenameOldArch, baosCloud.toByteArray(),
                                baosIndCloud.toByteArray(), baosCloudApp.toByteArray(), baosIndCloudApp.toByteArray(),
                                cloudFound, indCloudFound, cloudAppFound, indCloudAppFound);
                    } else {
                        return downloadFile(subscriber, organization, filenameOldArch, filePath);
                    }
                }
            } catch (BlobStorageException e) {
                handleBlobStorageException(e, filePath);
            } catch (IOException e) {
                throw new G4itRestException("500", String.format("Error processing file %s", filePath), e);
            }
        }

        final String filePath = String.join("/", subscriber, organization.toString(), FileFolder.EXPORT.getFolderName(), filename);
        return downloadFile(subscriber, organization, filename, filePath);
    }

    private ResponseEntity<Resource> downloadFile(String subscriber, Long organization,
                                                  String filename, String filePath) {
        try {
            InputStream inputStream = fileSystemService.downloadFile(subscriber, organization, FileFolder.EXPORT, filename);
            return ResponseEntity.ok(new InputStreamResource(inputStream));
        } catch (BlobStorageException e) {
            handleBlobStorageException(e, filePath);
        } catch (FileNotFoundException e) {
            throw new G4itRestException("404", String.format(FILE_NOT_FOUND, filePath));
        } catch (IOException e) {
            throw new G4itRestException("500", String.format(ERROR_DOWNLOADING_FILE, filePath), e);
        }
        return null;
    }

    private void handleBlobStorageException(BlobStorageException e, String filePath) {
        if (e.getErrorCode().equals(BlobErrorCode.BLOB_NOT_FOUND)) {
            throw new G4itRestException("404", String.format(FILE_NOT_FOUND, filePath));
        }
        throw new G4itRestException("500", String.format(ERROR_DOWNLOADING_FILE, filePath), e);
    }

    private ResponseEntity<Resource> processZipContent(String subscriber, Long organization, String filenameOldArch,
                                                       byte[] cloudData, byte[] indCloudData,
                                                       byte[] cloudAppData, byte[] indAppData,
                                                       boolean cloudFound, boolean indCloudFound,
                                                       boolean appCloudFound, boolean indAppCloudFound) throws IOException {
        try (InputStream oldArchiveInputStream = fileSystemService.downloadFile(subscriber,
                organization, FileFolder.EXPORT, filenameOldArch);
             ZipInputStream oldArchiveZis = new ZipInputStream(oldArchiveInputStream);
             ByteArrayOutputStream updatedZipBaos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(updatedZipBaos)) {

            copyExistingEntries(oldArchiveZis, zos);
            addCloudEntries(zos, cloudData, indCloudData, cloudAppData, indAppData,
                    cloudFound, indCloudFound, appCloudFound, indAppCloudFound);

            byte[] finalZipContent = updatedZipBaos.toByteArray();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + filenameOldArch + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(new InputStreamResource(new ByteArrayInputStream(finalZipContent)));
        }
    }

    private void copyExistingEntries(ZipInputStream oldArchiveZis, ZipOutputStream zos) throws IOException {
        ZipEntry archiveEntry;
        byte[] buffer = new byte[8192];
        while ((archiveEntry = oldArchiveZis.getNextEntry()) != null) {
            ZipEntry newEntry = new ZipEntry(archiveEntry.getName());
            zos.putNextEntry(newEntry);
            int len;
            while ((len = oldArchiveZis.read(buffer)) > 0) {
                zos.write(buffer, 0, len);
            }
            zos.closeEntry();
        }
    }

    private void addCloudEntries(ZipOutputStream zos, byte[] cloudCsvData, byte[] indCsvData,
                                 byte[] cloudAppData, byte[] indAppData,
                                 boolean cloudFound, boolean indCloudFound,
                                 boolean appCloudFound, boolean indAppCloudFound) throws IOException {
        if (cloudFound) {
            ZipEntry cloudCsvEntry = new ZipEntry("cloud_virtual_equipment.csv");
            cloudCsvEntry.setSize(cloudCsvData.length);
            zos.putNextEntry(cloudCsvEntry);
            zos.write(cloudCsvData);
            zos.closeEntry();
        }
        if (indCloudFound) {
            ZipEntry indCsvEntry = new ZipEntry("ind_cloud_virtual_equipment.csv");
            indCsvEntry.setSize(indCsvData.length);
            zos.putNextEntry(indCsvEntry);
            zos.write(indCsvData);
            zos.closeEntry();
        }
        if (appCloudFound) {
            ZipEntry cloudAppEntry = new ZipEntry("cloud_application.csv");
            cloudAppEntry.setSize(cloudAppData.length);
            zos.putNextEntry(cloudAppEntry);
            zos.write(cloudAppData);
            zos.closeEntry();
        }
        if (indAppCloudFound) {
            ZipEntry indAppEntry = new ZipEntry("ind_cloud_application.csv");
            indAppEntry.setSize(indAppData.length);
            zos.putNextEntry(indAppEntry);
            zos.write(indAppData);
            zos.closeEntry();
        }
        zos.finish();
    }


}
