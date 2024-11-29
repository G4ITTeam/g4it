/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apidigitalservice.modeldb;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@SqlResultSetMapping(
        name = "DigitalServiceNetworkIndicatorMapping",
        classes = @ConstructorResult(
                targetClass = DigitalServiceNetworkIndicatorView.class,
                columns = {
                        @ColumnResult(name = "id", type = Long.class),
                        @ColumnResult(name = "criteria"),
                        @ColumnResult(name = "network_type"),
                        @ColumnResult(name = "raw_value", type = Float.class),
                        @ColumnResult(name = "sip_value", type = Float.class),
                        @ColumnResult(name = "unit"),
                        @ColumnResult(name = "status"),
                        @ColumnResult(name = "count_value", type = Long.class)
                }
        )
)
@NamedNativeQuery(
        name = "DigitalServiceNetworkIndicatorView.findDigitalServiceNetworkIndicators",
        resultSetMapping = "DigitalServiceNetworkIndicatorMapping",
        query = """
                    select
                        row_number() OVER ()                                        AS id,
                        ep.critere                                                  as criteria,
                        rf.description                                              as network_type,
                        sum(ep.impact_unitaire)                                     as raw_value,
                        sum(ep.impact_unitaire/sip.individual_sustainable_package)  as sip_value,
                        ep.unite                                                    as unit,
                        ep.statut_indicateur                                        as status,
                        count(*)                                                    as count_value
                    from
                        ind_indicateur_impact_equipement_physique ep
                    inner join
                        network n
                    on
                        n.uid = ep.nom_equipement
                    inner join
                        ref_network_type rf
                    on
                        n.network_type = rf.id
                    inner join
                        ref_sustainable_individual_package sip
                    on
                        sip.criteria = ep.critere
                    where
                        ep.nom_lot = :uid
                        and
                        ep.type_equipement = 'Network'
                    group by
                        ep.critere,
                        rf.description,
                        ep.unite,
                        ep.statut_indicateur
                """
)
@Entity
@Data
@SuperBuilder
@AllArgsConstructor
public class DigitalServiceNetworkIndicatorView implements Serializable {

    @Id
    private Long id;

    private String criteria;

    private String networkType;

    private Float rawValue;

    private Float sipValue;

    private String unit;

    private String status;

    private Long countValue;
}
