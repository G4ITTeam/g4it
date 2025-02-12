/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class LocalFileUtils {

    private LocalFileUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * delete folder recursively
     *
     * @param path the folder path
     */
    public static void cleanFolder(String path) {
        if (path == null) return;

        Path pathVar = Path.of(path);
        if (Files.exists(pathVar)) {
            IOException e = null;
            for (int i = 0; i < 3; i++) {
                try {
                    log.info("Deleting local folder {}", path);
                    FileSystemUtils.deleteRecursively(pathVar);
                    return;
                } catch (IOException ex) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignored) {
                        log.error("Error occurred in deleting folder {}, error: {}", path, ignored.getMessage());
                    }
                    e = ex;
                }
            }
            log.error("Cannot delete folder {}, error: {}", path, e.getMessage());
        }
    }
}
