/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.common.utils;

public enum ExportBatchStatus {
    STARTED,
    LOADING_SIP_REFERENTIAL,
    EXPORTING_DATA,
    UPLOADING_DATA,
    CLEANING_WORKING_FOLDERS,
    EXPORT_GENERATED,
    REMOVED
}