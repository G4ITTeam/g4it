/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@Slf4j
public class LocalFileDeletionService {

    @Value("${local.working.folder}")
    private String localWorkingFolder;

    private static final Long TEN_MINUTE_IN_MS = 10 * 60 * 1000L;

    public void executeDeletion() {
        File[] files = Path.of(localWorkingFolder).resolve("referential").toFile().listFiles();
        if (files != null) {
            for (File file : files) {
                if ((System.currentTimeMillis() - file.lastModified()) > TEN_MINUTE_IN_MS) {
                    try {
                        Files.delete(file.toPath());
                    } catch (IOException e) {
                        log.error("File {} cannot be deleted", file.getName(), e);
                    }
                }
            }
        }
    }
}
