/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apiuser.model;

import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * Organization Business Object.
 */
@Data
@SuperBuilder
public class OrganizationBO {

    /**
     * The Organization's name.
     */
    private String name;

    /**
     * The 'default' flag.
     */
    private boolean defaultFlag;

    /**
     * User roles on organization.
     */
    private List<String> roles;
}
