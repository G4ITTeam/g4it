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
        name = "InPhysicalEquipmentLowImpactIndicatorsMapping",
        classes = @ConstructorResult(
                targetClass = InPhysicalEquipmentLowImpactView.class,
                columns = {
                        @ColumnResult(name = "id", type = Long.class),
                        @ColumnResult(name = "inventory_id", type = Long.class),
                        @ColumnResult(name = "inventory_name"),
                        @ColumnResult(name = "country"),
                        @ColumnResult(name = "type"),
                        @ColumnResult(name = "nom_entite"),
                        @ColumnResult(name = "statut"),
                        @ColumnResult(name = "quantite", type = Integer.class),
                        @ColumnResult(name = "low_impact", type = Boolean.class)
                }
        )
)

@NamedNativeQuery(
        name = "InPhysicalEquipmentLowImpactView.findPhysicalEquipmentLowImpactIndicatorsByOrgId",
        resultSetMapping = "InPhysicalEquipmentLowImpactIndicatorsMapping",
        query = """
                 SELECT Row_number()
                          OVER ()                                                    AS id,
                          inv.id                                                     AS inventory_id,
                          inv.NAME                                                   AS inventory_name,
                          COALESCE(NULLIF(dc."location" , ''), ep."location")        AS country,
                          ep.type                                                    AS type,
                          ep.filters[1]                                              AS statut,
                          ep.common_filters[1]                                       AS nom_entite,
                          Sum(Cast(ep.quantity AS INT))                              AS quantite,
                          false                                                      AS low_impact
                   FROM   in_physical_equipment ep
                          INNER JOIN inventory inv
                            ON inv.id = ep.inventory_id
                          LEFT JOIN in_datacenter dc
                            ON ep.inventory_id = dc.inventory_id
                            AND ep.datacenter_name = dc."name"
                   WHERE inv.id = :inventoryId
                   GROUP  BY inv.id,
                     inv.NAME,
                     dc.location,
                     ep.location,
                     ep.type,
                     ep.filters,
                     ep.common_filters
                """)
@Data
@Entity
@SuperBuilder
@AllArgsConstructor
public class InPhysicalEquipmentLowImpactView implements Serializable {

    @Id
    private Long id;

    private Long inventoryId;

    private String inventoryName;

    private String country;

    private String type;

    private String nomEntite;

    private String statut;

    private Integer quantite;

    private Boolean lowImpact;
}
