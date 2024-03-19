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
        name = "EquipmentFiltersMapping",
        classes = @ConstructorResult(
                targetClass = EquipmentFilters.class,
                columns = {
                        @ColumnResult(name = "id", type = Long.class),
                        @ColumnResult(name = "country"),
                        @ColumnResult(name = "entity"),
                        @ColumnResult(name = "type"),
                        @ColumnResult(name = "status")
                }
        )
)
@NamedNativeQuery(name = "EquipmentFilters.getFiltersByInventoryId", resultSetMapping = "EquipmentFiltersMapping", query = """
            WITH filters AS (
                select
                    localisation        AS country,
                    nom_entite          AS entity,
                    ''                  AS type,
                    ''                  AS status
                FROM data_center
                WHERE inventory_id = :inventoryId
                UNION
                SELECT
                    pays_utilisation    AS country,
                    nom_entite          AS entity,
                    type                AS type,
                    statut              AS status
                FROM equipement_physique
                WHERE inventory_id = :inventoryId
                UNION
                SELECT
                    ''                                          AS country,
                    COALESCE(ep.nom_entite, '')                 AS entity,
                    ep.type_equipement                          AS type,
                    COALESCE(ep.statut_equipement_physique, '') AS status
                FROM ind_indicateur_impact_equipement_physique ep
                WHERE ep.nom_lot = :batchName
                GROUP BY country, type, entity, status
            )
            SELECT DISTINCT
                ROW_NUMBER() OVER ()                                    AS id,
                country,
                entity,
                REGEXP_REPLACE(type, CONCAT(:organization, '_'), '')    AS type,
                status
            FROM filters
        """)
@Data
@Entity
@SuperBuilder
@AllArgsConstructor
public class EquipmentFilters implements Serializable {

    @Id
    private long id;

    private String country;

    private String entity;

    private String type;

    private String status;

}

