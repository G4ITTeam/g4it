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
 * Virtual Server Impact business object.
 */
@Data
@SuperBuilder
@NoArgsConstructor
public class VirtualServerImpactBO {

    /**
     * Virtual server name.
     */
    private String name;

    /**
     * Sip value.
     */
    private Double sipValue;

    /**
     * Quantity.
     */
    private Integer quantity;

    /**
     * Raw value.
     */
    private Double rawValue;

    /**
     * Unit
     */
    private String unit;
}
