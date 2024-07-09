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
        name = "PhysicalEquipmentAvgAgeIndicatorsMapping",
        classes = @ConstructorResult(
                targetClass = PhysicalEquipmentAvgAgeView.class,
                columns = {
                        @ColumnResult(name = "id", type = Long.class),
                        @ColumnResult(name = "inventory_id", type = Long.class),
                        @ColumnResult(name = "inventory_name"),
                        @ColumnResult(name = "country"),
                        @ColumnResult(name = "type"),
                        @ColumnResult(name = "nom_entite"),
                        @ColumnResult(name = "statut"),
                        @ColumnResult(name = "poids", type = Integer.class),
                        @ColumnResult(name = "age_moyen", type = Double.class)
                }
        )
)

@NamedNativeQuery(name = "PhysicalEquipmentAvgAgeView.findPhysicalEquipmentAvgAgeIndicators", resultSetMapping = "PhysicalEquipmentAvgAgeIndicatorsMapping", query = """
        SELECT
            ROW_NUMBER() OVER ()                                        AS id,
            inv.id                                                      AS inventory_id,
            inv.name                                                    AS inventory_name,
            coalesce(nullif(dc.localisation,''), ep.pays_utilisation)   AS country,
            ep.type                                                     AS type,
            ep.nom_entite                                               AS nom_entite,
            ep.statut                                                   AS statut,
            SUM(CAST(ep.quantite as int))                               AS poids,
            SUM((
                (COALESCE(CAST(NULLIF(ep.date_retrait,'') AS DATE), CURRENT_DATE) - CAST(ep.date_achat AS DATE))/365.0)
                *
                CAST(ep.quantite AS numeric)
                )
                /
            SUM(CAST(ep.quantite as numeric))                           AS age_moyen
            FROM
                equipement_physique ep
            INNER JOIN inventory inv
                ON inv.id = ep.inventory_id
            LEFT JOIN data_center dc
                ON ep.inventory_id = dc.inventory_id
                AND ep.nom_court_datacenter = dc.nom_court_datacenter
            WHERE ep.date_achat NOT LIKE ''
            AND (ep.date_retrait LIKE '' OR (CAST(ep.date_achat AS DATE) <= CAST(ep.date_retrait AS DATE)))
            AND inv.id = :inventoryId
            GROUP BY
                inv.id,
                inv.name,
                dc.localisation,
                ep.pays_utilisation,
                ep.type,
                ep.nom_entite,
                ep.statut
        """)
@Data
@Entity
@SuperBuilder
@AllArgsConstructor
public class PhysicalEquipmentAvgAgeView implements Serializable {

    @Id
    private Long id;

    private Long inventoryId;

    private String inventoryName;

    private String country;

    private String type;

    private String nomEntite;

    private String statut;

    private Integer poids;

    private Double ageMoyen;
}
