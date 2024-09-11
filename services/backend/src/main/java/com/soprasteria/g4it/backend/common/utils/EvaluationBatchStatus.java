/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.common.utils;

public enum EvaluationBatchStatus {
    DATA_EXTRACTION,
    DATA_EXPOSITION_TO_NUMECOVAL,
    CALCUL_SUBMISSION_TO_NUMECOVAL,
    CALCUL_IN_PROGRESS,
    AGGREGATION_IN_PROGRESS
}
