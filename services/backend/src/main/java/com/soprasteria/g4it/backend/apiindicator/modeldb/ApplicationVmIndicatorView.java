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
 * Application VM Indicator view to retrieve the virtual machine link to an inventory application indicators.
 */
@SqlResultSetMapping(
        name = "ApplicationVmIndicatorsMapping",
        classes = @ConstructorResult(
                targetClass = ApplicationVmIndicatorView.class,
                columns = {
                        @ColumnResult(name = "id", type = Long.class),
                        @ColumnResult(name = "criteria"),
                        @ColumnResult(name = "life_cycle"),
                        @ColumnResult(name = "environment"),
                        @ColumnResult(name = "equipment_type"),
                        @ColumnResult(name = "cluster"),
                        @ColumnResult(name = "vm_name"),
                        @ColumnResult(name = "impact", type = Double.class),
                        @ColumnResult(name = "unit"),
                        @ColumnResult(name = "sip", type = Double.class)
                }
        )
)
@NamedNativeQuery(name = "ApplicationVmIndicatorView.findIndicators", resultSetMapping = "ApplicationVmIndicatorsMapping", query = """
        SELECT ROW_NUMBER() OVER ()                                          AS id,
            ia.critere                                                       AS criteria,
            ia.etapeacv                                                      AS life_cycle,
            ia.type_environnement                                            AS environment,
            REGEXP_REPLACE(ep.type, CONCAT(o.name, '_'), '')                 AS equipment_type,
            COALESCE(ev.cluster, '')                                         AS "cluster",
            ev.nom_vm                                                        AS vm_name,
            SUM(ia.impact_unitaire)                                          AS impact,
            ia.unite                                                         AS unit,
            SUM(ia.impact_unitaire / ref_sip.individual_sustainable_package) AS sip
        FROM ind_indicateur_impact_application ia
        INNER JOIN g4it_evaluation_report er
            ON er.batch_name = ia.nom_lot
        INNER JOIN inventory i
            ON i.id = er.inventory_id
        INNER JOIN g4it_organization o
            ON i.organization_id = o.id
        INNER JOIN ref_sustainable_individual_package ref_sip
            ON ref_sip.criteria  = ia.critere
        CROSS JOIN equipement_physique ep
        CROSS JOIN equipement_virtuel ev
        WHERE ep.inventory_id = :inventoryId
        AND ev.inventory_id = :inventoryId
        AND ia.critere = :criteria
        AND ia.nom_application = :applicationName
        AND ia.statut_indicateur = 'OK'
        AND ia.nom_lot = :batchName
        AND ia.nom_equipement_physique = ep.nom_equipement_physique
        AND ia.nom_equipement_physique = ev.nom_equipement_physique
        AND ia.nom_organisation = :organization
        AND ia.nom_equipement_virtuel = ev.nom_vm
        GROUP BY
            ia.critere,
            ia.etapeacv,
            ia.type_environnement,
            ep.type,
            ev.cluster,
            ev.nom_vm,
            ia.unite,
            o.name;
        """)
@Data
@Entity
@SuperBuilder
@AllArgsConstructor
public class ApplicationVmIndicatorView implements Serializable {

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
     * The application environment.
     */
    private String environment;

    /**
     * The linked equipment type.
     */
    private String equipmentType;

    /**
     * The linked cluster.
     */
    private String cluster;

    /**
     * The linked vm name.
     */
    private String vmName;

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
