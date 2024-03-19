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
 * The application filter business object.
 */
@Data
@NoArgsConstructor
@SuperBuilder
public class ApplicationFiltersBO {

    /**
     * The distinct environment list.
     */
    private List<String> environments = new ArrayList<>();

    /**
     * The distinct life cycle list.
     */
    private List<String> lifeCycles = new ArrayList<>();

    /**
     * The distinct domain list.
     */
    private List<ApplicationDomainsFiltersBO> domains = new ArrayList<>();

    /**
     * The distinct type list.
     */
    private List<String> types = new ArrayList<>();

}
