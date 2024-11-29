/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.common.task.model;

public enum TaskStatus {
    TO_START,
    IN_PROGRESS,
    FAILED,
    COMPLETED,
    COMPLETED_WITH_ERRORS,
    REMOVED
}