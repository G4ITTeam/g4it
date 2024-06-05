/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiindicator.modeldb;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * Equipment Indicator view to retrieve the inventory equipment indicators.
 */
@SqlResultSetMapping(
        name = "EquipmentIndicatorsMapping",
        classes = @ConstructorResult(
                targetClass = EquipmentIndicatorView.class,
                columns = {
                        @ColumnResult(name = "id", type = Long.class),
                        @ColumnResult(name = "criteria"),
                        @ColumnResult(name = "acv_step"),
                        @ColumnResult(name = "entity"),
                        @ColumnResult(name = "equipment"),
                        @ColumnResult(name = "status"),
                        @ColumnResult(name = "country"),
                        @ColumnResult(name = "batch_name"),
                        @ColumnResult(name = "organization"),
                        @ColumnResult(name = "impact", type = Double.class),
                        @ColumnResult(name = "unit"),
                        @ColumnResult(name = "sip", type = Double.class)
                }
        )
)
@NamedNativeQuery(name = "EquipmentIndicatorView.findIndicators", resultSetMapping = "EquipmentIndicatorsMapping", query = """
            SELECT ROW_NUMBER() OVER ()                                                                                     AS id,
                ind_indicateur_impact_equipement_physique.critere                                                           AS criteria,
                ind_indicateur_impact_equipement_physique.etapeacv                                                          AS acv_step,
                ind_indicateur_impact_equipement_physique.nom_entite                                                        AS entity,
                REGEXP_REPLACE(ind_indicateur_impact_equipement_physique.type_equipement, CONCAT(:organization, '_'), '')   AS equipment,
                ind_indicateur_impact_equipement_physique.statut_equipement_physique                                        AS status,
                CASE
                    WHEN en_equipement_physique.nom_court_datacenter <> ''
                        AND en_equipement_physique.nom_court_datacenter IS NOT NULL
                        AND data_center.localisation <> ''
                        AND data_center.localisation IS NOT NULL
                            THEN data_center.localisation
                    ELSE en_equipement_physique.pays_utilisation
                END AS country,
                ind_indicateur_impact_equipement_physique.nom_lot                                                           AS batch_name,
                ind_indicateur_impact_equipement_physique.nom_organisation                                                  AS organization,
                SUM(ind_indicateur_impact_equipement_physique.impact_unitaire)                                              AS impact,
                ind_indicateur_impact_equipement_physique.unite                                                             AS unit,
                SUM(ind_indicateur_impact_equipement_physique.impact_unitaire / ref_sip.individual_sustainable_package)     AS sip
            FROM ind_indicateur_impact_equipement_physique
            CROSS JOIN en_equipement_physique
            INNER JOIN REF_SUSTAINABLE_INDIVIDUAL_PACKAGE ref_sip
                ON ref_sip.criteria  = ind_indicateur_impact_equipement_physique.critere
            LEFT JOIN EN_DATA_CENTER data_center
                ON data_center.nom_organisation = en_equipement_physique.nom_organisation
                AND data_center.nom_lot = en_equipement_physique.nom_lot
                AND data_center.nom_court_datacenter = en_equipement_physique.nom_court_datacenter
            WHERE ind_indicateur_impact_equipement_physique.statut_indicateur = 'OK'
            AND ind_indicateur_impact_equipement_physique.nom_lot = :batchName
            AND ind_indicateur_impact_equipement_physique.nom_organisation = :organization
            AND ind_indicateur_impact_equipement_physique.nom_equipement = en_equipement_physique.nom_equipement_physique
            AND en_equipement_physique.nom_lot = :batchName
            AND en_equipement_physique.nom_organisation = :organization
            GROUP BY
                ind_indicateur_impact_equipement_physique.critere,
                ind_indicateur_impact_equipement_physique.etapeacv,
                country,
                ind_indicateur_impact_equipement_physique.type_equipement,
                ind_indicateur_impact_equipement_physique.nom_entite,
                ind_indicateur_impact_equipement_physique.statut_equipement_physique,
                ind_indicateur_impact_equipement_physique.nom_lot,
                ind_indicateur_impact_equipement_physique.nom_organisation,
                ind_indicateur_impact_equipement_physique.unite;
        """)
@Data
@Entity
@SuperBuilder
@AllArgsConstructor
public class EquipmentIndicatorView implements Serializable {

    @Id
    private Long id;

    private String criteria;

    private String acvStep;

    private String entity;

    private String equipment;

    private String status;

    private String country;

    private String batchName;

    private String organization;

    private Double impact;

    private String unit;

    private Double sip;

}
