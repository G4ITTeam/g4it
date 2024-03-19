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

import java.util.List;

/**
 * Servers Impact.
 */
@Data
@SuperBuilder
@NoArgsConstructor
public class ServersImpactBO {

    /**
     * Physical server name.
     */
    private String name;

    /**
     * Total Sip Value.
     */
    private Double totalSipValue;

    /**
     * Host efficiency.
     */
    private String hostingEfficiency;

    /**
     * Virtual servers impacts.
     */
    private List<VirtualServerImpactBO> impactVmDisk;

    /**
     * Impact step.
     */
    private List<ImpactStepBO> impactStep;
}
