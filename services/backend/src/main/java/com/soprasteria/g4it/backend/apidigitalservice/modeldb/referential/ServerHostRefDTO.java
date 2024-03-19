/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apidigitalservice.modeldb.referential;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
public class ServerHostRefDTO {

    /**
     * NumEcoEval Reference.
     */
    private Long id;

    /**
     * Device's description.
     */
    private String description;

    /**
     * Server host type.
     */
    private String type;

    /**
     * Number of VCpu.
     */
    private Integer nbOfVcpu;

    /**
     * Total disk (in GB).
     */
    private Integer totalDisk;

    /**
     * Device's lifespan.
     */
    private Double lifespan;

    /**
     * Annual electricity consumption
     */
    private Integer annualElectricityConsumption;
}
