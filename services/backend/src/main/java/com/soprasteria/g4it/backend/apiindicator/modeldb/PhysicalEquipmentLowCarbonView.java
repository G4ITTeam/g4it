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
        name = "PhysicalEquipmentLowCarbonIndicatorsMapping",
        classes = @ConstructorResult(
                targetClass = PhysicalEquipmentLowCarbonView.class,
                columns = {
                        @ColumnResult(name = "id", type = Long.class),
                        @ColumnResult(name = "inventory_id", type = Long.class),
                        @ColumnResult(name = "organisation"),
                        @ColumnResult(name = "inventory_name"),
                        @ColumnResult(name = "pays_utilisation"),
                        @ColumnResult(name = "type"),
                        @ColumnResult(name = "nom_entite"),
                        @ColumnResult(name = "statut"),
                        @ColumnResult(name = "quantite", type = Integer.class),
                        @ColumnResult(name = "low_carbon", type = Boolean.class)
                }
        )
)
@NamedNativeQuery(name = "PhysicalEquipmentLowCarbonView.findPhysicalEquipmentLowCarbonIndicators", resultSetMapping = "PhysicalEquipmentLowCarbonIndicatorsMapping", query = """
            WITH equipement_physique_temp AS (
                SELECT
                    inv.id as inventory_id,
                    org.name                                                    AS organisation,
                    inv.name                                                    AS inventory_name,
                    COALESCE(NULLIF(dc.localisation,''), ep.pays_utilisation)   AS pays_utilisation,
                    REGEXP_REPLACE(ep.type, CONCAT(:organization, '_'), '')     AS type,
                    ep.nom_entite,
                    ep.statut,
                    SUM(CAST(ep.quantite as int))                               AS quantite
                FROM equipement_physique ep
                INNER JOIN inventory inv
                    ON inv.id = ep.inventory_id
                INNER JOIN g4it_organization org
                    ON org.id = inv.organization_id
                INNER JOIN g4it_subscriber sub
                ON sub.id = org.subscriber_id
                LEFT JOIN data_center dc
                    ON ep.inventory_id = dc.inventory_id
                    AND ep.nom_court_datacenter = dc.nom_court_datacenter
                WHERE org.name = :organization
                AND sub.name = :subscriber
                AND inv.id = :inventoryId
                GROUP BY
                    inv.id,
                    org.name,
                    inv.name,
                    dc.localisation,
                    ep.pays_utilisation,
                    ep.type,
                    ep.nom_entite,
                    ep.statut
            )
            SELECT
                ROW_NUMBER() OVER ()                AS id,
                ep_temp.*,
                CASE
                    WHEN ref_factcaract.valeur < 0.1 THEN true
                    ELSE false
                END                                 AS low_carbon
                FROM equipement_physique_temp ep_temp
                LEFT JOIN ref_facteurcaracterisation  ref_factcaract
                    ON ep_temp.pays_utilisation = ref_factcaract.localisation
                    AND nomcritere = 'Climate change'
                    AND ref_factcaract.categorie = 'electricity-mix'
        """)
@Data
@Entity
@SuperBuilder
@AllArgsConstructor
public class PhysicalEquipmentLowCarbonView implements Serializable {

    @Id
    private Long id;

    private Long inventoryId;

    private String organisation;

    private String inventoryName;

    private String paysUtilisation;

    private String type;

    private String nomEntite;

    private String statut;

    private Integer quantite;

    private Boolean lowCarbon;
}
