/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiindicator.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Server Impact Step.
 */
@Data
@SuperBuilder
@NoArgsConstructor
public class ImpactStepBO {

    /**
     * Acv Step.
     */
    private String acvStep;

    /**
     * Sip Value.
     */
    private Double sipValue;

    /**
     * Raw value.
     */
    private Double rawValue;

    /**
     * Unit
     */
    private String unit;
}
