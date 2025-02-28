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
        name = "InPhysicalEquipmentAvgAgeIndicatorsMapping",
        classes = @ConstructorResult(
                targetClass = InPhysicalEquipmentAvgAgeView.class,
                columns = {
                        @ColumnResult(name = "id", type = Long.class),
                        @ColumnResult(name = "country"),
                        @ColumnResult(name = "type"),
                        @ColumnResult(name = "nom_entite"),
                        @ColumnResult(name = "statut"),
                        @ColumnResult(name = "poids", type = Double.class),
                        @ColumnResult(name = "age_moyen", type = Double.class)
                }
        )
)

@NamedNativeQuery(name = "InPhysicalEquipmentAvgAgeView.findPhysicalEquipmentAvgAgeIndicators",
        resultSetMapping = "InPhysicalEquipmentAvgAgeIndicatorsMapping", query = """
        SELECT
          ROW_NUMBER() OVER ()          AS id,
          "location"                    AS country,
          equipment_type                AS type,
          common_filters[1]             AS nom_entite,
          filters[1]                    AS statut,
          sum(quantity)                 AS poids,
          sum(lifespan) / sum(quantity) AS age_moyen
        FROM out_physical_equipment ope
        WHERE task_id = :taskId
        AND criterion = 'CLIMATE_CHANGE'
        AND lifecycle_step = 'USING'
        AND status_indicator = 'OK'
        GROUP BY
            ope.location,
            ope.equipment_type,
            ope.common_filters,
            ope.filters;
        """)
@Data
@Entity
@SuperBuilder
@AllArgsConstructor
public class InPhysicalEquipmentAvgAgeView implements Serializable {

    @Id
    private Long id;

    private String country;

    private String type;

    private String nomEntite;

    private String statut;

    private Double poids;

    private Double ageMoyen;
}
