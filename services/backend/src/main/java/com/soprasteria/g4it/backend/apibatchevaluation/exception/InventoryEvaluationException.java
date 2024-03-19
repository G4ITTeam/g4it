/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apibatchevaluation.exception;

public class InventoryEvaluationException extends Exception {
    public InventoryEvaluationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
