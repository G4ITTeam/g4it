/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.scheduler;

import com.soprasteria.g4it.backend.apibatchexport.business.InventoryExportService;
import com.soprasteria.g4it.backend.apidigitalservice.business.DigitalServiceService;
import com.soprasteria.g4it.backend.apiinventory.business.InventoryDeleteService;
import com.soprasteria.g4it.backend.apiinventory.repository.InventoryRepository;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import com.soprasteria.g4it.backend.apiuser.repository.OrganizationRepository;
import com.soprasteria.g4it.backend.common.filesystem.business.FileDeletionService;
import com.soprasteria.g4it.backend.common.filesystem.model.FileFolder;
import com.soprasteria.g4it.backend.common.utils.OrganizationStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class OrganizationDeletionService {

    @Autowired
    InventoryExportService inventoryExportService;

    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private InventoryRepository inventoryRepository;
    @Autowired
    private InventoryDeleteService inventoryDeleteService;
    @Autowired
    private DigitalServiceService digitalServiceService;

    @Autowired
    private FileDeletionService fileDeletionService;

    /**
     * Execute the deletion
     * Get all organizations with status 'TO_BE_DELETED'
     * Execute the deletion for data and storage files
     */
    public void executeDeletion() {
        final long start = System.currentTimeMillis();
        final LocalDateTime now = LocalDateTime.now();
        int nbInventoriesDeleted = 0;
        int nbDigitalServicesDeleted = 0;
        List<String> deletedFilePaths = new ArrayList<>();

        List<Organization> organizations = organizationRepository.findAllByStatusIn(List.of(OrganizationStatus.TO_BE_DELETED.name()));

        for (Organization organizationEntity : organizations) {
            final String subscriber = organizationEntity.getSubscriber().getName();
            final Long organizationId = organizationEntity.getId();

            final int dataRetentionDay = now.isAfter(organizationEntity.getDeletionDate()) ? 0 : -1;
            if (dataRetentionDay == 0) {
                log.info("Deleting data of {}/{}", subscriber, organizationEntity.getName());
                // Delete Inventories
                nbInventoriesDeleted += inventoryRepository.findByOrganization(organizationEntity).stream()
                        .mapToInt(inventory -> {
                            inventoryDeleteService.deleteInventory(subscriber, organizationId, inventory.getId());
                            return 1;
                        })
                        .sum();

                // Delete Digital services
                nbDigitalServicesDeleted += digitalServiceService.getAllDigitalServicesByOrganization(organizationId).stream()
                        .mapToInt(digitalServiceBO -> {
                            digitalServiceService.deleteDigitalService(digitalServiceBO.getUid());
                            return 1;
                        })
                        .sum();

                // Delete Export Files from storage
                List<String> deletedExportFilePaths = fileDeletionService.deleteFiles(subscriber, organizationId.toString(), FileFolder.EXPORT, dataRetentionDay);

                // Update Export Batch Status in database
                deletedExportFilePaths.forEach(fileName -> inventoryExportService.updateBatchStatusCodeToRemove(fileName));
                deletedFilePaths.addAll(deletedExportFilePaths);

                // Delete Output Files from storage
                List<String> deletedOutputFilePaths = fileDeletionService.deleteFiles(subscriber, organizationId.toString(), FileFolder.OUTPUT, dataRetentionDay);
                deletedFilePaths.addAll(deletedOutputFilePaths);

                // update organization status to "INACTIVE" if status is "TO_BE_DELETED"
                if (organizationEntity.getStatus().equals(OrganizationStatus.TO_BE_DELETED.name())) {
                    organizationRepository.setStatusForOrganization(organizationEntity.getId(), OrganizationStatus.INACTIVE.name());
                    log.info("Update status of {}/{} to {} ", subscriber, organizationEntity.getName(), OrganizationStatus.INACTIVE.name());
                }
            }
        }
        log.info("Deletion of {} inventories , {} digital-services  and {} files - {} , execution time={} ms for organization marked as {}",
                nbInventoriesDeleted,
                nbDigitalServicesDeleted,
                deletedFilePaths.size(),
                deletedFilePaths,
                System.currentTimeMillis() - start,
                OrganizationStatus.TO_BE_DELETED.name()
        );
    }

}
