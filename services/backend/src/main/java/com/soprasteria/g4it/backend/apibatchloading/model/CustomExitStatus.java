/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apibatchloading.model;

import org.springframework.batch.core.ExitStatus;

/**
 * Custom Exit Status
 */
public class CustomExitStatus extends ExitStatus {

    public static final CustomExitStatus COMPLETED_WITH_ERRORS = new CustomExitStatus("COMPLETED_WITH_ERRORS", "Integration errors occurs during the job");
    public static final CustomExitStatus COMPLETED = new CustomExitStatus("COMPLETED", "No integration error detected");

    public CustomExitStatus(final String exitCode, final String exitDescription) {
        super(exitCode, exitDescription);
    }
}
