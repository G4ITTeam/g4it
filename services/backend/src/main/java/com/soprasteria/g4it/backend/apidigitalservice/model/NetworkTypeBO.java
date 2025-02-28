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

/**
 * Network Type Business Object.
 */
@Data
@SuperBuilder
@RequiredArgsConstructor
public class NetworkTypeBO {

    /**
     * Network reference.
     */
    private String code;

    /**
     * Type value.
     */
    private String value;

    /**
     * Network type.
     */
    private String type;

    /**
     * Annual quantity of GigaOctet
     */
    private Integer annualQuantityOfGo;

    /**
     * Country.
     */
    private String country;

}
