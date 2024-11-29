/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apibatchexport.exception;

public class ExportRuntimeException extends RuntimeException {

    public ExportRuntimeException(final String message) {
        super(message);
    }

    public ExportRuntimeException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
