/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.scheduler;

import com.soprasteria.g4it.backend.apibatchexport.business.InventoryExportService;
import com.soprasteria.g4it.backend.apiuser.business.OrganizationService;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import com.soprasteria.g4it.backend.apiuser.repository.OrganizationRepository;
import com.soprasteria.g4it.backend.common.filesystem.business.FileDeletionService;
import com.soprasteria.g4it.backend.common.filesystem.model.FileFolder;
import com.soprasteria.g4it.backend.common.utils.OrganizationStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class StorageDeletionService {

    @Autowired
    InventoryExportService inventoryExportService;
    @Value("${g4it.storage.retention.day.export}")
    private Integer storageRetentionDayExport;
    @Value("${g4it.storage.retention.day.output}")
    private Integer storageRetentionDayOutput;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private FileDeletionService fileDeletionService;
    @Autowired
    private OrganizationService organizationService;

    /**
     * Execute the deletion
     * Get all subscribers and organizations
     * Execute the deletion for output and work folders
     */
    public void executeDeletion() {

        final long start = System.currentTimeMillis();
        // Fetch Organization with 'ACTIVE' status only.
        List<Organization> organizations = organizationRepository.findAllByStatusIn(List.of(OrganizationStatus.ACTIVE.name()));

        List<String> deletedFilePaths = new ArrayList<>();
        for (Organization organizationEntity : organizations) {
            final String subscriber = organizationEntity.getSubscriber().getName();
            final String organization = organizationEntity.getName();

            // organization > subscriber > default
            final Integer retentionExport = Optional.ofNullable(organizationEntity.getStorageRetentionDayExport())
                    .orElse(Optional.ofNullable(organizationEntity.getSubscriber().getStorageRetentionDayExport())
                            .orElse(storageRetentionDayExport));

            List<String> deletedExportFilePaths = fileDeletionService.deleteFiles(subscriber, organization, FileFolder.EXPORT, retentionExport);
            // Update Export Batch Status in database
            deletedExportFilePaths.forEach(fileName -> inventoryExportService.updateBatchStatusCodeToRemove(fileName));
            deletedFilePaths.addAll(deletedExportFilePaths);
            // organization > subscriber > default
            final Integer retentionOutput = Optional.ofNullable(organizationEntity.getStorageRetentionDayOutput())
                    .orElse(Optional.ofNullable(organizationEntity.getSubscriber().getStorageRetentionDayOutput())
                            .orElse(storageRetentionDayOutput));

            List<String> deletedOutputFilePaths = fileDeletionService.deleteFiles(subscriber, organization, FileFolder.OUTPUT, retentionOutput);
            deletedFilePaths.addAll(deletedOutputFilePaths);
        }

        log.info("Deletion of {} files - {}, execution time={} ms", deletedFilePaths.size(), deletedFilePaths, System.currentTimeMillis() - start);
    }


}
