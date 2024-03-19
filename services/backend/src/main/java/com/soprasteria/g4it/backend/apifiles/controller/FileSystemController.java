/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apifiles.controller;

import com.soprasteria.g4it.backend.apifiles.business.FileSystemService;
import com.soprasteria.g4it.backend.common.filesystem.model.FileType;
import com.soprasteria.g4it.backend.server.gen.api.FileSystemApiDelegate;
import com.soprasteria.g4it.backend.server.gen.api.dto.FileDescriptionRest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * FileSystem service to access file storage.
 */
@Slf4j
@Service
public class FileSystemController implements FileSystemApiDelegate {

    /**
     * File System Service.
     */
    @Autowired
    private FileSystemService fileSystemService;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<FileDescriptionRest>> listFiles(final String subscriber,
                                                               final String organization,
                                                               final Long inventoryId) {
        try {
            return ResponseEntity.ok().body(fileSystemService.listFiles(subscriber, organization));
        } catch (final IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error occurred while get file: " + e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Map<String, List<String>>> uploadCSV(final String subscriber,
                                                               final String organization,
                                                               final Long inventoryId,
                                                               final List<MultipartFile> datacenters,
                                                               final List<MultipartFile> applications,
                                                               final List<MultipartFile> physicalEquipments,
                                                               final List<MultipartFile> virtualEquipments) {

        List<String> datacenterFiles = fileSystemService.manageFiles(subscriber, organization, datacenters);
        List<String> physicalEquipmentFiles = fileSystemService.manageFiles(subscriber, organization, physicalEquipments);
        List<String> virtualEquipmentFiles = fileSystemService.manageFiles(subscriber, organization, virtualEquipments);
        List<String> applicationFiles = fileSystemService.manageFiles(subscriber, organization, applications);

        return ResponseEntity.ok(Map.of(
                FileType.DATACENTER.getValue(), datacenterFiles,
                FileType.APPLICATION.getValue(), applicationFiles,
                FileType.EQUIPEMENT_PHYSIQUE.getValue(), physicalEquipmentFiles,
                FileType.EQUIPEMENT_VIRTUEL.getValue(), virtualEquipmentFiles
        ));
    }

}
