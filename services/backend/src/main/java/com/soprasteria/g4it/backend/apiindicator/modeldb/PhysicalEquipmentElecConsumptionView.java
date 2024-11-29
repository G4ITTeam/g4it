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

@SqlResultSetMapping(
        name = "PhysicalEquipmentElecConsumptionIndicatorsMapping",
        classes = @ConstructorResult(
                targetClass = PhysicalEquipmentElecConsumptionView.class,
                columns = {
                        @ColumnResult(name = "id", type = Long.class),
                        @ColumnResult(name = "nom_entite"),
                        @ColumnResult(name = "type"),
                        @ColumnResult(name = "statut"),
                        @ColumnResult(name = "country"),
                        @ColumnResult(name = "elec_consumption", type = Double.class)
                }
        )
)

@NamedNativeQuery(name = "PhysicalEquipmentElecConsumptionView.findPhysicalEquipmentElecConsumptionIndicators", resultSetMapping = "PhysicalEquipmentElecConsumptionIndicatorsMapping", query = """
        SELECT Row_number()
                 OVER ()                                        AS id,
               ind_ep.nom_entite                                AS nom_entite,
               ind_ep.type_equipement                           AS type,
               ind_ep.statut_equipement_physique                AS statut,
               CASE
                 WHEN ep.nom_court_datacenter <> ''
                      AND ep.nom_court_datacenter IS NOT NULL
                      AND dc.localisation <> ''
                      AND dc.localisation IS NOT NULL THEN dc.localisation
                 ELSE ep.pays_utilisation
               END                                              AS country,
               Sum(ind_ep.conso_elec_moyenne) / :criteriaNumber AS elec_consumption
        FROM   ind_indicateur_impact_equipement_physique AS ind_ep
               CROSS JOIN en_equipement_physique AS ep
               LEFT JOIN en_data_center dc
                      ON dc.nom_lot = ep.nom_lot
                         AND dc.nom_court_datacenter = ep.nom_court_datacenter
        WHERE  ind_ep.statut_indicateur = 'OK'
               AND ind_ep.etapeacv = 'UTILISATION'
               AND ind_ep.nom_lot = :batchName
               AND ind_ep.nom_equipement = ep.nom_equipement_physique
               AND ep.nom_lot = :batchName
        GROUP  BY country,
                  ind_ep.type_equipement,
                  ind_ep.nom_entite,
                  ind_ep.statut_equipement_physique;
        """)
@Data
@Entity
@SuperBuilder
@AllArgsConstructor
public class PhysicalEquipmentElecConsumptionView implements Serializable {

    @Id
    private Long id;

    private String nomEntite;

    private String type;

    private String statut;

    private String country;

    private Double elecConsumption;
}
