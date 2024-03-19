/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apidigitalservice.model;

import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * DataCenter Business Object.
 */
@Data
@SuperBuilder
public class ServerDataCenterBO {

    /**
     * Unique identifier.
     */
    private String uid;

    /**
     * Datacenter's name.
     */
    private String name;

    /**
     * Localtion.
     */
    private String location;

    /**
     * Power usage effectiveness.
     */
    private BigDecimal pue;
}
