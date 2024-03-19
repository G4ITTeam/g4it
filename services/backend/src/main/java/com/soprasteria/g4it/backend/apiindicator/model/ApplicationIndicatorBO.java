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

import java.util.List;

/**
 * The application indicators business object.
 */
@Data
@SuperBuilder
@NoArgsConstructor
public class ApplicationIndicatorBO<T extends ApplicationImpactBO> {

    /**
     * The indicator criteria.
     */
    private String criteria;

    /**
     * The indicator unit.
     */
    private String unit;

    /**
     * Application impacts.
     */
    private List<T> impacts;
}
