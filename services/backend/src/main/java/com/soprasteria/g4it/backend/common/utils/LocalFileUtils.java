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

    /**
     * delete folder recursively
     *
     * @param path the folder path
     */
    public static void cleanFolder(String path) {
        if (path == null) return;

        if (Files.exists(Path.of(path))) {
            IOException e = null;
            for (int i = 0; i < 3; i++) {
                try {
                    log.info("Deleting local folder {}", path);
                    FileSystemUtils.deleteRecursively(Path.of(path));
                    return;
                } catch (IOException ex) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignored) {
                    }
                    e = ex;
                }
            }
            log.error("Cannot delete folder {}, error: {}", path, e.getMessage());
        }
    }
}
