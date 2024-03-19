/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apiindicator.modeldb.numecoeval;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Application indicator to export.
 */
@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@IdClass(ApplicationIndicatorKey.class)
@Table(name = "ind_indicateur_impact_application")
public class ApplicationIndicator extends CommonIndicatorExport {

    @Id
    @Column(name = "nom_lot")
    private String batchName;

    @Id
    @Column(name = "etapeacv")
    private String acvStep;

    @Id
    @Column(name = "date_lot_discriminator")
    private LocalDate batchDateDiscriminator;

    @Id
    @Column(name = "nom_organisation_discriminator")
    private String organizationNameDiscriminator;

    @Id
    @Column(name = "nom_source_donnee_discriminator")
    private String dataSourceNameDiscriminator;

    @Id
    @Column(name = "nom_equipement_physique")
    private String physicalEquipmentName;

    @Id
    @Column(name = "nom_equipement_virtuel")
    private String virtualEquipmentName;

    @Id
    @Column(name = "nom_application")
    private String applicationName;

    @Id
    @Column(name = "type_environnement")
    private String environmentType;

    @Id
    @Column(name = "nom_entite_discriminator")
    private String entityNameDiscriminator;

    @Column(name = "date_calcul")
    private LocalDateTime calculationDate;

    @Column(name = "date_lot")
    private LocalDate batchDate;

    @Column(name = "nom_organisation")
    private String organizationName;

    @Column(name = "nom_source_donnee")
    private String dataSourceName;

    @Column(name = "nom_entite")
    private String entityName;

    @Column(name = "source")
    private String source;

    @Column(name = "statut_indicateur")
    private String indicatorStatus;

    @Column(name = "trace")
    private String trace;

    @Column(name = "version_calcul")
    private String calculationVersion;

    @Column(name = "domaine")
    private String domain;

    @Column(name = "sous_domaine")
    private String subDomain;

    @Column(name = "unite")
    private String unit;

    @Column(name = "conso_elec_moyenne")
    private Double averagePowerConsumption;

}
