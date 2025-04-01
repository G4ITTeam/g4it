/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apifiles.controller;

import com.azure.storage.blob.models.BlobErrorCode;
import com.azure.storage.blob.models.BlobStorageException;
import com.soprasteria.g4it.backend.apifiles.business.FileSystemService;
import com.soprasteria.g4it.backend.apiinventory.business.InventoryService;
import com.soprasteria.g4it.backend.apiinventory.repository.InventoryRepository;
import com.soprasteria.g4it.backend.common.filesystem.model.FileFolder;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import com.soprasteria.g4it.backend.server.gen.api.FileSystemApiDelegate;
import com.soprasteria.g4it.backend.server.gen.api.dto.FileDescriptionRest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

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
     * InventoryService
     */
    @Autowired
    private InventoryService inventoryService;

    /**
     * InventoryRepository
     */
    @Autowired
    private InventoryRepository inventoryRepository;

    /**
     * Task repository
     */
    @Autowired
    private TaskRepository taskRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<FileDescriptionRest>> listFiles(final String subscriber,
                                                               final Long organization,
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
    public ResponseEntity<Resource> downloadResultsFile(@PathVariable final String subscriber,
                                                        @PathVariable final Long organization,
                                                        @PathVariable final Long inventoryId,
                                                        @PathVariable final String taskId) {

        String filename = String.join("/", taskId, Constants.REJECTED_FILES_ZIP);

        final String filePath = String.join("/", subscriber, organization.toString(), FileFolder.OUTPUT.getFolderName(), filename);

        try {
            InputStream inputStream = fileSystemService.downloadFile(subscriber, organization, FileFolder.OUTPUT, filename);
            return ResponseEntity.ok(new InputStreamResource(inputStream));
        } catch (BlobStorageException e) {
            if (e.getErrorCode().equals(BlobErrorCode.BLOB_NOT_FOUND)) {
                throw new G4itRestException("404", String.format("file %s not found in filestorage", filePath));
            } else {
                throw new G4itRestException("500", String.format("Something went wrong downloading file %s", filePath), e);
            }
        } catch (FileNotFoundException e) {
            throw new G4itRestException("404", String.format("file %s not found in filestorage", filePath));
        } catch (IOException e) {
            throw new G4itRestException("500", String.format("Something went wrong downloading file %s", filePath), e);
        }

    }

}
