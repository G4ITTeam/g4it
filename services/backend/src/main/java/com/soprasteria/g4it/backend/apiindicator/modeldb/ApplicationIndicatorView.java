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
 * Application Indicator view to retrieve the inventory application indicators.
 */

@SqlResultSetMapping(
        name = "ApplicationIndicatorsMapping",
        classes = @ConstructorResult(
                targetClass = ApplicationIndicatorView.class,
                columns = {
                        @ColumnResult(name = "id", type = Long.class),
                        @ColumnResult(name = "criteria"),
                        @ColumnResult(name = "life_cycle"),
                        @ColumnResult(name = "domain"),
                        @ColumnResult(name = "sub_domain"),
                        @ColumnResult(name = "environment"),
                        @ColumnResult(name = "equipment_type"),
                        @ColumnResult(name = "application_name"),
                        @ColumnResult(name = "impact", type = Double.class),
                        @ColumnResult(name = "unit"),
                        @ColumnResult(name = "sip", type = Double.class)
                }
        )
)
@NamedNativeQuery(name = "ApplicationIndicatorView.findIndicators", resultSetMapping = "ApplicationIndicatorsMapping", query = """
        SELECT ROW_NUMBER() OVER ()                                          AS id,
            ia.critere                                                       AS criteria,
            ia.etapeacv                                                      AS life_cycle,
            COALESCE(ia.domaine, '')                                         AS "domain",
            COALESCE(ia.sous_domaine, '')                                    AS sub_domain,
            ia.type_environnement                                            AS environment,
            ep.type                                                          AS equipment_type,
            ia.nom_application                                               AS application_name,
            SUM(ia.impact_unitaire)                                          AS impact,
            ia.unite                                                         AS unit,
            SUM(ia.impact_unitaire / ref_sip.individual_sustainable_package) AS sip
        FROM ind_indicateur_impact_application ia
        INNER JOIN ref_sustainable_individual_package ref_sip
            ON ref_sip.criteria  = ia.critere
        CROSS JOIN equipement_physique ep
        WHERE ia.statut_indicateur = 'OK'
        AND ep.inventory_id = :inventoryId
        AND ia.nom_lot = :batchName
        AND ia.nom_equipement_physique = ep.nom_equipement_physique
        GROUP BY
            ia.critere,
            ia.etapeacv,
            ia.domaine,
            ia.sous_domaine,
            ia.type_environnement,
            ep.type,
            ia.nom_application,
            ia.unite,
            ia.nom_organisation;
        """)

@Data
@Entity
@SuperBuilder
@AllArgsConstructor
public class ApplicationIndicatorView implements Serializable {

    /**
     * Unique identifier.
     */
    @Id
    private Long id;

    /**
     * The indicator criteria.
     */
    private String criteria;

    /**
     * The life cycle.
     */
    private String lifeCycle;

    /**
     * The application domain.
     */
    private String domain;

    /**
     * The application sub domain.
     */
    private String subDomain;

    /**
     * The application environment.
     */
    private String environment;

    /**
     * The linked equipment type.
     */
    private String equipmentType;

    /**
     * The application name.
     */
    private String applicationName;

    /**
     * The indicator impact value.
     */
    private Double impact;

    /**
     * The indicator unit.
     */
    private String unit;

    /**
     * The sustainable impact value.
     */
    private Double sip;

}
