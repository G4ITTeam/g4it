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
        name = "DataCenterIndicatorsMapping",
        classes = @ConstructorResult(
                targetClass = DataCenterIndicatorView.class,
                columns = {
                        @ColumnResult(name = "id", type = Long.class),
                        @ColumnResult(name = "data_center_name"),
                        @ColumnResult(name = "inventory_name"),
                        @ColumnResult(name = "country"),
                        @ColumnResult(name = "entity"),
                        @ColumnResult(name = "equipment"),
                        @ColumnResult(name = "status"),
                        @ColumnResult(name = "pue", type = Float.class),
                        @ColumnResult(name = "physical_equipment_count", type = Integer.class)
                }
        )
)
@NamedNativeQuery(name = "DataCenterIndicatorView.findDataCenterIndicators", resultSetMapping = "DataCenterIndicatorsMapping", query = """
            SELECT
                ROW_NUMBER() OVER ()             AS id,
                dc.nom_court_datacenter          AS data_center_name,
                inv.name                         AS inventory_name,
                dc.localisation                  AS country,
                dc.nom_entite                    AS entity,
                ep.type                          AS equipment,
                ep.statut                        AS status,
                NULLIF(dc.pue, '')               AS pue,
                SUM(cast(ep.quantite as int))    AS physical_equipment_count
            FROM data_center dc
            INNER JOIN inventory inv
                ON dc.inventory_id = inv.id
            LEFT JOIN equipement_physique ep
                ON dc.inventory_id = ep.inventory_id
                AND dc.nom_court_datacenter = ep.nom_court_datacenter
            WHERE inv.id = :inventoryId
            GROUP BY
                inv.name,
                dc.nom_court_datacenter,
                ep.type,
                ep.statut,
                dc.nom_entite,
                dc.localisation,
                dc.pue
        """)

@Data
@Entity
@SuperBuilder
@AllArgsConstructor
public class DataCenterIndicatorView implements Serializable {

    @Id
    private Long id;

    private String dataCenterName;

    private String inventoryName;

    private String country;

    private String entity;

    private String equipment;

    private String status;

    private Float pue;

    private Integer physicalEquipmentCount;

}
