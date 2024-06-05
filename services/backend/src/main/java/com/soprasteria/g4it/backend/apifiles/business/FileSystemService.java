/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apifiles.business;

import com.azure.storage.blob.models.BlobStorageException;
import com.soprasteria.g4it.backend.apibatchloading.mapper.FileDescriptionRestMapper;
import com.soprasteria.g4it.backend.common.filesystem.model.FileFolder;
import com.soprasteria.g4it.backend.common.filesystem.model.FileStorage;
import com.soprasteria.g4it.backend.common.filesystem.model.FileSystem;
import com.soprasteria.g4it.backend.common.utils.SanitizeUrl;
import com.soprasteria.g4it.backend.exception.BadRequestException;
import com.soprasteria.g4it.backend.server.gen.api.dto.FileDescriptionRest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
@Slf4j
public class FileSystemService {

    /**
     * Content types.
     */
    private static final List<String> TYPES = List.of("text/csv", "application/vnd.ms-excel");
    /**
     * File System.
     */
    @Autowired
    private FileSystem fileSystem;
    /**
     * File System.
     */
    @Autowired
    private FileDescriptionRestMapper fileDescriptionRestMapper;

    /**
     * List files of subscriber and organization in INPUT directory
     *
     * @param subscriber   the subscriber
     * @param organization the organization
     * @return the list of files
     */
    public List<FileDescriptionRest> listFiles(final String subscriber, final String organization) throws IOException {
        return fileDescriptionRestMapper.toDto(fetchStorage(subscriber, organization).listFiles(FileFolder.INPUT));
    }

    /**
     * List files of subscriber and organization in INPUT directory
     *
     * @param subscriber   the subscriber
     * @param organization the organization
     * @return the list of files
     */
    public List<FileDescriptionRest> listFiles(final String subscriber, final String organization, final FileFolder fileFolder) throws IOException {
        return fileDescriptionRestMapper.toDto(fetchStorage(subscriber, organization).listFiles(fileFolder));
    }

    /**
     * List files of subscriber and organization in INPUT directory
     *
     * @param subscriber   the subscriber
     * @param organization the organization
     * @return the list of files
     */
    public InputStream downloadFile(final String subscriber, final String organization, final FileFolder fileFolder, final String filename) throws IOException {
        return fetchStorage(subscriber, organization).readFile(fileFolder, filename);
    }

    /**
     * Manage files :
     * - check files
     * - get fileStorage
     * - upload each file into fileStorage
     *
     * @param subscriber   the subscriber
     * @param organization the organization
     * @param files        the list of file
     * @return the fileName list uploaded
     */
    public List<String> manageFiles(final String subscriber, final String organization, final List<MultipartFile> files) {
        if (files == null) return List.of();
        checkFiles(files);
        FileStorage fileStorage = fetchStorage(subscriber, organization);
        return files.stream().map(file -> this.uploadFile(file, fileStorage)).toList();
    }

    /**
     * Check files types, throws a BadRequestException when one type is wrong
     *
     * @param files the input files
     * @throws BadRequestException when one type is wrong
     */
    private void checkFiles(final List<MultipartFile> files) {
        if (files == null) return;

        files.stream()
                .filter(file -> !TYPES.contains(file.getContentType()))
                .findFirst()
                .ifPresent(wrongFile -> {
                    throw new BadRequestException(
                            wrongFile.getOriginalFilename(),
                            String.format("Format is not in types %s. Actual type is %s", TYPES, wrongFile.getContentType())
                    );
                });

    }

    /**
     * Retrieve the storage associated with organization
     *
     * @param subscriber   the client subscriber.
     * @param organization the organization as known by G4IT
     * @return the filestorage associated with this subscriber
     * @throws ResponseStatusException Not Found if storage is unknown
     */
    private FileStorage fetchStorage(final String subscriber, final String organization) {
        final FileStorage storage = fileSystem.mount(subscriber, organization);
        if (storage == null) {
            log.info("No storage found for organization {}", organization);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Storage not found for organization " + organization);
        }
        return storage;
    }

    /**
     * Puts file on file storage for an organization.
     *
     * @param file        the file to put.
     * @param fileStorage the fileStorage
     * @return the file path.
     */
    private String uploadFile(final MultipartFile file, final FileStorage fileStorage) {
        try {
            return fileStorage.upload(FileFolder.INPUT, file.getOriginalFilename(), file.getName(), file.getBytes());
        } catch (final IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error occurred while uploading file: " + e.getMessage());
        }
    }

    /**
     * Get the filename from url and nb slash (0 or 1)
     * Example:
     * - getFilenameFromUrl("/path/to/file.txt", 0) returns "file.txt"
     * - getFilenameFromUrl("/path/to/file.txt", 1) returns "to/file.txt"
     *
     * @param url     the url
     * @param nbSlash the number of slash present in return
     * @return the filename
     */
    public String getFilenameFromUrl(final String url, final int nbSlash) {
        String fileUrl = SanitizeUrl.removeTokenAndEncoding(url);
        final String[] split = fileUrl.split("/");
        return switch (nbSlash) {
            case 0 -> split[split.length - 1];
            case 1 -> split.length < 2 ? fileUrl : split[split.length - 2] + "/" + split[split.length - 1];
            default -> fileUrl;
        };
    }


    /**
     * Delete file for subscriber, organization, fileFolder
     *
     * @param subscriber   the subscriber
     * @param organization the organization
     * @param fileFolder   the fileFolder
     * @param fileUrl      the fileUrl
     */
    public String deleteFile(String subscriber, String organization, FileFolder fileFolder, String fileUrl) {
        String fileName = getFilenameFromUrl(fileUrl, 0);
        String deletedFilePath = null;
        final FileStorage fileStorage = fileSystem.mount(subscriber, organization);
        try {
            deletedFilePath = fileStorage.getFileUrl(fileFolder, fileName);
            fileStorage.delete(fileFolder, fileName);
        } catch (BlobStorageException e) {
            if (e.getStatusCode() == 404) {
                log.error("An error occurred during file deletion: file {} not found in storage as it should get file: {}", deletedFilePath, fileUrl);
            } else {
                log.error("An error occurred during deletion of file", e);
            }
        } catch (IOException e) {
            log.error("An error occurred during deletion of file", e);
        }
        return deletedFilePath;
    }

}
