/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.common.evaluating.business;

import com.soprasteria.g4it.backend.common.filesystem.business.FileStorage;
import com.soprasteria.g4it.backend.common.filesystem.business.FileSystem;
import com.soprasteria.g4it.backend.common.filesystem.business.local.LocalFileService;
import com.soprasteria.g4it.backend.common.filesystem.model.FileFolder;
import com.soprasteria.g4it.backend.exception.AsyncTaskException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class ExportZipService {

    @Autowired
    FileSystem fileSystem;

    @Autowired
    LocalFileService localFileService;

    @Value("${local.working.folder}")
    private String localWorkingFolder;


    /**
     * Upload export zip file
     *
     * @param taskId         the task
     * @param subscriber     the subscriber
     * @param organizationId the organizationId
     */
    public void uploadZip(Long taskId, String subscriber, String organizationId) {
        FileStorage fileStorage = fileSystem.mount(subscriber, organizationId);
        try {
            final Path exportPath = Path.of(localWorkingFolder).resolve("export").resolve(String.valueOf(taskId));
            if (Files.exists(exportPath) && !localFileService.isEmpty(exportPath)) {
                // create rejected zip file
                final File exportZipFile = localFileService.createZipFile(exportPath, exportPath.resolve(taskId + ".zip"));

                // send zip to file storage
                fileStorage.upload(exportZipFile.getAbsolutePath(), FileFolder.EXPORT, taskId + ".zip");

                // clear directory
                FileSystemUtils.deleteRecursively(exportPath);
            }
        } catch (IOException e) {
            throw new AsyncTaskException("An error occurred on file upload of rejected zip file", e);
        }
    }


}
