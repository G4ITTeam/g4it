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
 * Terminal Business Object.
 */
@Data
@SuperBuilder
@RequiredArgsConstructor
public class TerminalBO {

    /**
     * Terminal ID.
     */
    private String uid;

    /**
     * Creation date.
     */
    @EqualsAndHashCode.Exclude
    private LocalDateTime creationDate;

    /**
     * Device type.
     */
    private DeviceTypeBO type;

    /**
     * Country.
     */
    private String country;

    /**
     * Number of users.
     */
    private Integer numberOfUsers;

    /**
     * Yearly usage time per user (in minute).
     */
    private Integer yearlyUsageTimePerUser;

}
