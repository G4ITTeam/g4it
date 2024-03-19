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
    name = "DigitalServiceTerminalIndicatorMapping",
    classes = @ConstructorResult(
        targetClass = DigitalServiceTerminalIndicatorView.class,
        columns = {
            @ColumnResult(name= "id", type = Long.class),
            @ColumnResult(name = "criteria"),
            @ColumnResult(name = "country"),
            @ColumnResult(name = "description"),
            @ColumnResult(name = "number_users", type = Long.class),
            @ColumnResult(name = "yearly_usage_time_per_user", type = Integer.class),
            @ColumnResult(name = "acv_step"),
            @ColumnResult(name = "raw_value", type = Float.class),
            @ColumnResult(name = "sip_value", type = Float.class)
        }
    )
)
@NamedNativeQuery(
    name = "DigitalServiceTerminalIndicatorView.findDigitalServiceTerminalIndicators",
    resultSetMapping = "DigitalServiceTerminalIndicatorMapping",
    query = """
    select
        row_number() OVER ()                                        AS id,
        ep.critere                                                  as criteria,
        t.country                                                   as country,
        rf.description                                              as description,
        avg(t.number_of_users)                                      as number_users,
        avg(t.yearly_usage_time_per_user)                            as yearly_usage_time_per_user,
        ep.etapeacv                                                 as acv_step,
        sum(ep.impact_unitaire)                                     as raw_value,
        sum(ep.impact_unitaire/sip.individual_sustainable_package)  as sip_value
    from
        ind_indicateur_impact_equipement_physique ep
    inner join
        terminal t
    on
        t.uid = ep.nom_equipement
    inner join
        ref_device_type rf
    on
        t.device_type = rf.id
    inner join
        ref_sustainable_individual_package sip
    on
        sip.criteria = ep.critere
    where
        ep.nom_lot = :uid
        and
        ep.nom_organisation = :organization
        and
        ep.type_equipement = 'Terminal'
    group by
        ep.critere,
        rf.description,
        t.country,
        ep.etapeacv,
        t.uid
    """
)
@Entity
@Data
@SuperBuilder
@AllArgsConstructor
public class DigitalServiceTerminalIndicatorView implements Serializable {

    @Id
    private Long id;

    private String criteria;

    private String country;

    private String description;

    private Long numberUsers;

    private Integer yearlyUsageTimePerUser;

    private String acvStep;

    private Float rawValue;

    private Float sipValue;
}
