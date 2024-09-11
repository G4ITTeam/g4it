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
 * Physical equipment indicator to export.
 */
@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@IdClass(PhysicalEquipmentIndicatorKey.class)
@Table(name = "ind_indicateur_impact_equipement_physique")
public class PhysicalEquipmentIndicator extends CommonIndicatorExport {

    @Id
    @Column(name = "nom_lot")
    private String batchName;

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
    @Column(name = "etapeacv")
    private String acvStep;

    @Id
    @Column(name = "nom_equipement")
    private String equipmentName;

    @Id
    @Column(name = "nom_entite_discriminator")
    private String entityNameDiscriminator;

    @Column(name = "date_calcul")
    private LocalDateTime calculationDate;

    @Column(name = "date_lot")
    private LocalDate batchDate;

    @Column(name = "source")
    private String source;

    @Column(name = "statut_indicateur")
    private String indicatorStatus;

    @Column(name = "trace")
    private String trace;

    @Column(name = "version_calcul")
    private String calculationVersion;

    @Column(name = "conso_elec_moyenne")
    private Double averagePowerConsumption;

    @Column(name = "quantite")
    private Integer quantity;

    @Column(name = "statut_equipement_physique")
    private String physicalEquipmentStatus;

    @Column(name = "type_equipement")
    private String equipmentType;

    @Column(name = "unite")
    private String unit;

    @Column(name = "nom_entite")
    private String entityName;

    @Column(name = "nom_organisation")
    private String organizationName;

    @Column(name = "nom_source_donnee")
    private String dataSourceName;

    /**
     * Used by csv-headers.yml
     *
     * @return the common criteria
     */
    public String getCommonCriteria() {
        return this.getCriteria();
    }

    /**
     * Used by csv-headers.yml
     *
     * @return the common unitImpact
     */
    public Double getCommonUnitImpact() {
        return this.getUnitImpact();
    }

    /**
     * Used by csv-headers.yml
     *
     * @return the common sipImpact
     */
    public Double getCommonSipImpact() {
        return this.getSipImpact();
    }

}
