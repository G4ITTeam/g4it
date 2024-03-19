/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apidigitalservice.model;

import com.soprasteria.g4it.backend.apiindicator.model.ServersImpactBO;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * Digital Service Server Impact.
 */
@Data
@SuperBuilder
@NoArgsConstructor
public class DigitalServiceServerImpactBO {

    /**
     * Type of server.
     */
    private String serverType;

    /**
     * Mutualization type.
     */
    private String mutualizationType;

    /**
     * Impact business object list.
     */
    private List<ServersImpactBO> servers;
}
