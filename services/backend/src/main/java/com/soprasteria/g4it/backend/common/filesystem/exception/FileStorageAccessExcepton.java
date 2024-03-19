/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.common.filesystem.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class FileStorageAccessExcepton extends RuntimeException {

    public FileStorageAccessExcepton(final Throwable cause) {
        super(cause);
    }

    public FileStorageAccessExcepton(final String message) {
        super(message);
    }
}
