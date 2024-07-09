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
 * Application filters mapping.
 * <p>
 * This model defines the {@link com.soprasteria.g4it.core.inventory.infrastructure.repository.jpa.ApplicationFiltersRepository#getFiltersByBatchName} query
 * and the ApplicationFiltersMapping mapping
 */
@SqlResultSetMapping(
        name = "ApplicationFiltersMapping",
        classes = @ConstructorResult(
                targetClass = ApplicationFilters.class, columns = {
                @ColumnResult(name = "id", type = Long.class),
                @ColumnResult(name = "environment"),
                @ColumnResult(name = "life_cycle"),
                @ColumnResult(name = "domain"),
                @ColumnResult(name = "sub_domain"),
                @ColumnResult(name = "type")
        }
        )
)
@NamedNativeQuery(
        name = "ApplicationFilters.getFiltersByBatchName",
        resultSetMapping = "ApplicationFiltersMapping",
        query = """
                    WITH filters AS (
                        SELECT DISTINCT
                            ia.type_environnement                                           AS environment,
                            ia.etapeacv                                                     AS life_cycle,
                            COALESCE(ia.domaine, '')                                        AS "domain",
                            COALESCE(ia.sous_domaine, '')                                   AS sub_domain,
                            ep.type                                                         AS "type"
                        FROM ind_indicateur_impact_application ia
                        INNER JOIN g4it_evaluation_report er on er.batch_name = ia.nom_lot
                        INNER JOIN inventory i on i.id = er.inventory_id
                        INNER JOIN equipement_physique ep on ep.inventory_id = i.id and ia.nom_equipement_physique = ep.nom_equipement_physique
                        WHERE ia.nom_lot = :batchName
                        AND i.id = :inventoryId
                    )
                    SELECT
                        ROW_NUMBER() OVER ()    AS id,
                        *
                    FROM filters;
                """
)

@NamedNativeQuery(
        name = "ApplicationFilters.getFiltersByBatchNameAndApplicationName",
        resultSetMapping = "ApplicationFiltersMapping",
        query = """
                    WITH filters AS (
                        SELECT DISTINCT
                            ia.type_environnement                                           AS environment,
                            ia.etapeacv                                                     AS life_cycle,
                            COALESCE(ia.domaine, '')                                        AS "domain",
                            COALESCE(ia.sous_domaine, '')                                   AS sub_domain,
                            ep.type                                                         AS "type"
                        FROM ind_indicateur_impact_application ia
                        INNER JOIN g4it_evaluation_report er on er.batch_name = ia.nom_lot
                        INNER JOIN inventory i on i.id = er.inventory_id
                        INNER JOIN equipement_physique ep on ep.inventory_id = i.id and ia.nom_equipement_physique = ep.nom_equipement_physique
                        WHERE ia.nom_lot = :batchName
                        AND i.id = :inventoryId
                        AND ia.nom_application = :applicationName
                    )
                    SELECT
                        ROW_NUMBER() OVER ()    AS id,
                        *
                    FROM filters;
                """
)
@Data
@Entity
@SuperBuilder
@AllArgsConstructor
public class ApplicationFilters implements Serializable {

    /**
     * Unique identifier.
     */
    @Id
    private long id;

    /**
     * Distinct environment in the ind_indicateur_impact_application table.
     */
    private String environment;

    /**
     * Distinct life_cycle in the ind_indicateur_impact_application table.
     */
    private String lifeCycle;

    /**
     * Distinct domaine in the ind_indicateur_impact_application table.
     */
    private String domain;

    /**
     * Distinct sous_domaine in the ind_indicateur_impact_application table.
     */
    private String subDomain;

    /**
     * Distinct type in the equipement_physique table.
     */
    private String type;

}

