/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiindicator.modeldb;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@SuperBuilder
@Entity
@Table(name = "agg_application_indicator")
public class AggApplicationIndicator implements Serializable {
    /**
     * Unique identifier.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String batchName;

    private String criteria;

    private String lifeCycle;

    private String domain;

    private String subDomain;

    private String environment;

    private String equipmentType;

    private String applicationName;

    private Double impact;

    private String unit;

    private String virtualEquipmentName;

    private String cluster;

    private Double sip;

    private String statusIndicator;

}