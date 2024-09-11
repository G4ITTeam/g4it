/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.scheduler;

import com.soprasteria.g4it.backend.apidigitalservice.business.DigitalServiceService;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceRepository;
import com.soprasteria.g4it.backend.apiinventory.business.InventoryDeleteService;
import com.soprasteria.g4it.backend.apiinventory.repository.InventoryRepository;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import com.soprasteria.g4it.backend.apiuser.repository.OrganizationRepository;
import com.soprasteria.g4it.backend.common.utils.OrganizationStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class DataDeletionService {

    @Value("${g4it.data.retention.day}")
    private Integer dataRetentiondDay;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private InventoryDeleteService inventoryDeleteService;

    @Autowired
    private DigitalServiceService digitalServiceService;

    @Autowired
    private DigitalServiceRepository digitalServiceRepository;

    /**
     * Execute the deletion
     * Get all subscribers and organizations
     * Execute the deletion for data in
     */
    public void executeDeletion() {
        final LocalDateTime now = LocalDateTime.now();
        int nbInventoriesDeleted = 0;
        int nbDigitalServicesDeleted = 0;
        final long start = System.currentTimeMillis();

        List<Organization> organizations = organizationRepository.findAllByStatusIn(List.of(OrganizationStatus.ACTIVE.name()));

        for (Organization organizationEntity : organizations) {
            final String subscriber = organizationEntity.getSubscriber().getName();
            final Long organizationId = organizationEntity.getId();

            // organization > subscriber > default
            final Integer retentionDay = Optional.ofNullable(organizationEntity.getDataRetentionDay())
                    .orElse(Optional.ofNullable(organizationEntity.getSubscriber().getDataRetentionDay())
                            .orElse(dataRetentiondDay));

            // Inventories
            nbInventoriesDeleted += inventoryRepository.findByOrganization(organizationEntity).stream()
                    .filter(inventory -> now.minusDays(retentionDay).isAfter(inventory.getLastUpdateDate()))
                    .mapToInt(inventory -> {
                        inventoryDeleteService.deleteInventory(subscriber, organizationId, inventory.getId());
                        return 1;
                    })
                    .sum();

            // Digital services
            nbDigitalServicesDeleted += digitalServiceRepository.findByOrganization(organizationEntity).stream()
                    .filter(digitalServiceBO -> now.minusDays(retentionDay).isAfter(digitalServiceBO.getLastUpdateDate()))
                    .mapToInt(digitalServiceBO -> {
                        digitalServiceService.deleteDigitalService(digitalServiceBO.getUid());
                        return 1;
                    })
                    .sum();
        }

        log.info("Deletion of {} inventories and {} digital-services in database, execution time={} ms", nbInventoriesDeleted, nbDigitalServicesDeleted, System.currentTimeMillis() - start);
    }

}
