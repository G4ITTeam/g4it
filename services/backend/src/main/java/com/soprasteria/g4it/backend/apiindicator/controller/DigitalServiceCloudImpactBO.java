/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiindicator.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class DigitalServiceCloudImpactBO {

    private String country;

    private String description;

    private String instanceType;

    private String cloudProvider;

    private String acvStep;

    private Double rawValue;

    private Double sipValue;

    private String unit;

    private String status;

    private Long countValue;

    private Double averageWorkLoad;

    private Double averageUsage;

    private Double quantity;
}
