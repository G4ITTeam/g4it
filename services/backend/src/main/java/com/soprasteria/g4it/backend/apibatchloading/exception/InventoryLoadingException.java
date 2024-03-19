/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apibatchloading.exception;

public class InventoryLoadingException extends RuntimeException {
    public InventoryLoadingException(String message, Throwable cause) {
        super(message, cause);
    }

    public InventoryLoadingException(String message) {
        super(message);
    }
}
