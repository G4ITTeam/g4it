/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.scheduler;

import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import com.soprasteria.g4it.backend.apiuser.repository.OrganizationRepository;
import com.soprasteria.g4it.backend.common.filesystem.business.FileDeletionService;
import com.soprasteria.g4it.backend.common.filesystem.model.FileFolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class StorageDeletionService {

    @Value("${g4it.storage.retention.day.export}")
    private Integer storageRetentionDayExport;
    @Value("${g4it.storage.retention.day.output}")
    private Integer storageRetentionDayOutput;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private FileDeletionService fileDeletionService;

    /**
     * Execute the deletion
     * Get all subscribers and organizations
     * Execute the deletion for output and work folders
     */
    public void executeDeletion() {

        int nbFiles = 0;

        final long start = System.currentTimeMillis();

        List<Organization> organizations = organizationRepository.findAll();

        for (Organization organizationEntity : organizations) {
            final String subscriber = organizationEntity.getSubscriber().getName();
            final String organization = organizationEntity.getName();

            // organization > subscriber > default
            final Integer retentionExport = Optional.ofNullable(organizationEntity.getStorageRetentionDayExport())
                    .orElse(Optional.ofNullable(organizationEntity.getSubscriber().getStorageRetentionDayExport())
                            .orElse(storageRetentionDayExport));

            nbFiles += fileDeletionService.deleteFiles(subscriber, organization, FileFolder.EXPORT, retentionExport);

            // organization > subscriber > default
            final Integer retentionOutput = Optional.ofNullable(organizationEntity.getStorageRetentionDayOutput())
                    .orElse(Optional.ofNullable(organizationEntity.getSubscriber().getStorageRetentionDayOutput())
                            .orElse(storageRetentionDayOutput));

            nbFiles += fileDeletionService.deleteFiles(subscriber, organization, FileFolder.OUTPUT, retentionOutput);
        }

        log.info("Deletion of {} files, execution time={} ms", nbFiles, System.currentTimeMillis() - start);
    }


}
