/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apidigitalservice.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * Server Host Business Object.
 */
@Data
@SuperBuilder
@RequiredArgsConstructor
public class ServerHostBO {

    /**
     * Code.
     */
    private long code;

    /**
     * Value.
     */
    private String value;

    /**
     * Default characteristics
     */
    private List<ServerCharacteristicBO> characteristic;
}
