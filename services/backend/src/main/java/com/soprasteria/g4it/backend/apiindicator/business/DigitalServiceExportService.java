/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiindicator.business;

import com.soprasteria.g4it.backend.apifiles.business.FileSystemService;
import com.soprasteria.g4it.backend.common.filesystem.business.local.LocalFileService;
import com.soprasteria.g4it.backend.common.filesystem.model.FileFolder;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Service
public class DigitalServiceExportService {

    @Value("${local.working.folder}")
    private String localWorkingFolder;

    @Autowired
    private LocalFileService localFileService;
    @Autowired
    private FileSystemService fileSystemService;

    @Autowired
    private TaskRepository taskRepository;

    @PostConstruct
    public void initFolder() throws IOException {
        Files.createDirectories(Path.of(localWorkingFolder, "export", "digital-service"));
    }

    /**
     * Create all csv files
     *
     * @param digitalServiceUid digital service uid
     * @param subscriber        subscriber name
     * @param organization      organization name
     * @return zip file containing all csv files
     * @throws IOException exception
     */
    public InputStream createFiles(final String digitalServiceUid, final String subscriber, final Long organization) throws IOException {
        log.info("Digital-service Export - {}", digitalServiceUid);
        Task task = taskRepository.findByDigitalServiceUid(digitalServiceUid)
                .orElseThrow(() -> new G4itRestException("404", "Digital service task not found"));
        String filename = task.getId() + Constants.ZIP;
        return fileSystemService.downloadFile(subscriber, organization, FileFolder.EXPORT, filename);
    }

}
