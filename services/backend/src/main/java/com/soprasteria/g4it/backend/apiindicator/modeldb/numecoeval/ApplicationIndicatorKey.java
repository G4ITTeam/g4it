/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apiindicator.modeldb.numecoeval;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@SuperBuilder
@NoArgsConstructor
public class ApplicationIndicatorKey implements Serializable {

    private String batchName;

    private LocalDate batchDateDiscriminator;

    private String organizationNameDiscriminator;

    private String acvStep;

    private String criteria;

    private String physicalEquipmentName;

    private String virtualEquipmentName;

    private String applicationName;

    private String environmentType;

    private String entityNameDiscriminator;

    private String dataSourceNameDiscriminator;

}
