/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Exception use in authorization process.
 */
@RequiredArgsConstructor
@Getter
public class AuthorizationException extends RuntimeException {

    /**
     * The HTTP return code.
     */
    private final int statusCode;

    /**
     * The error message.
     */
    private final String message;
}
