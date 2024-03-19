/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apidigitalservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;


/**
 * Digital Service Business Object.
 */
@Data
@SuperBuilder
@AllArgsConstructor
public class DigitalServiceBO {

    /**
     * Unique identifier.
     */
    private String uid;

    /**
     * Name.
     */
    private String name;

    /**
     * User who created the digital service.
     */
    private String userName;

    /**
     * Creation date.
     */
    @EqualsAndHashCode.Exclude
    private LocalDateTime creationDate;

    /**
     * Last update date.
     */
    @EqualsAndHashCode.Exclude
    private LocalDateTime lastUpdateDate;

    /**
     * Last calculation date.
     */
    @EqualsAndHashCode.Exclude
    private LocalDateTime lastCalculationDate;

    /**
     * Terminals.
     */
    private List<TerminalBO> terminals;

    /**
     * Networks.
     */
    private List<NetworkBO> networks;

    /**
     * Servers
     */
    private List<ServerBO> servers;

}
