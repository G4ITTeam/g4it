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
        name = "PhysicalEquipmentLowImpactIndicatorsMapping",
        classes = @ConstructorResult(
                targetClass = PhysicalEquipmentLowImpactView.class,
                columns = {
                        @ColumnResult(name = "id", type = Long.class),
                        @ColumnResult(name = "inventory_id", type = Long.class),
                        @ColumnResult(name = "inventory_name"),
                        @ColumnResult(name = "pays_utilisation"),
                        @ColumnResult(name = "type"),
                        @ColumnResult(name = "nom_entite"),
                        @ColumnResult(name = "statut"),
                        @ColumnResult(name = "quantite", type = Integer.class),
                        @ColumnResult(name = "low_impact", type = Boolean.class)
                }
        )
)

@NamedNativeQuery(
        name = "PhysicalEquipmentLowImpactView.findPhysicalEquipmentLowImpactIndicatorsByOrgId",
        resultSetMapping = "PhysicalEquipmentLowImpactIndicatorsMapping",
        query = """
                SELECT Row_number()
                       OVER ()                                                    AS id,
                       inv.id                                                     AS inventory_id,
                       inv.NAME                                                   AS inventory_name,
                       COALESCE(NULLIF(dc.localisation, ''), ep.pays_utilisation) AS pays_utilisation,
                       ep.type                                                    AS type,
                       ep.nom_entite,
                       ep.statut,
                       Sum(Cast(ep.quantite AS INT))                              AS quantite,
                       false                                                      AS low_impact
                FROM   equipement_physique ep
                       INNER JOIN inventory inv
                               ON inv.id = ep.inventory_id
                       LEFT JOIN data_center dc
                              ON ep.inventory_id = dc.inventory_id
                                 AND ep.nom_court_datacenter = dc.nom_court_datacenter
                WHERE inv.id = :inventoryId
                GROUP  BY inv.id,
                          inv.NAME,
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
public class PhysicalEquipmentLowImpactView implements Serializable {

    @Id
    private Long id;

    private Long inventoryId;

    private String inventoryName;

    private String paysUtilisation;

    private String type;

    private String nomEntite;

    private String statut;

    private Integer quantite;

    private Boolean lowImpact;
}
