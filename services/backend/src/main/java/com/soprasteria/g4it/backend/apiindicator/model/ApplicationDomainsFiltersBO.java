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

import java.util.ArrayList;
import java.util.List;

/**
 * The application domain filter business object.
 */
@Data
@NoArgsConstructor
@SuperBuilder
public class ApplicationDomainsFiltersBO {

    /**
     * The domain name.
     */
    private String name;

    /**
     * The distinct sub domain list.
     */
    private List<String> subDomains = new ArrayList<>();
}
