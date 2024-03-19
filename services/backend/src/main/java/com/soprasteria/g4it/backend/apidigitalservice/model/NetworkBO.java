/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apidigitalservice.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * Network Business Object.
 */
@Data
@SuperBuilder
@RequiredArgsConstructor
public class NetworkBO {

    /**
     * Network ID.
     */
    private String uid;

    /**
     * Creation date.
     */
    @EqualsAndHashCode.Exclude
    private LocalDateTime creationDate;

    /**
     * Network type.
     */
    private NetworkTypeBO type;

    /**
     * Yearly quantity of gibaByte exchanged.
     */
    private Double yearlyQuantityOfGbExchanged;

}
