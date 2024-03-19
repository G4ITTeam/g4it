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
 * Invalid Referential exception.
 */
@RequiredArgsConstructor
public class InvalidReferentialException extends RuntimeException {

    /**
     * Referential Code in error.
     */
    @Getter
    private final String referentialInErrorCode;
}
