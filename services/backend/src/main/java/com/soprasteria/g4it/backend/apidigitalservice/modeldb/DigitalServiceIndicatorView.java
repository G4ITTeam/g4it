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
    name = "DigitalServiceIndicatorMapping",
    classes = @ConstructorResult(
        targetClass = DigitalServiceIndicatorView.class,
        columns = {
            @ColumnResult(name = "id", type = Long.class),
            @ColumnResult(name = "tier"),
            @ColumnResult(name = "criteria"),
            @ColumnResult(name = "unit"),
            @ColumnResult(name = "unit_value", type = Float.class),
            @ColumnResult(name = "sip_value", type = Float.class)
        }
    )
)
@NamedNativeQuery(name = "DigitalServiceIndicatorView.findDigitalServiceIndicators", resultSetMapping = "DigitalServiceIndicatorMapping", query = """
        with indicator_virtual_and_physical_equipment as (
            select
                case
                    when ep.type_equipement = 'Dedicated Server'
                        then 'Server'
                    else ep.type_equipement
                end                                                        as tier,
                ep.critere                                                 as criteria,
                ep.unite                                                   as unit,
                sum(ep.impact_unitaire)                                    as unit_value,
                sum(ep.impact_unitaire/sip.individual_sustainable_package) as sip_value
            from ind_indicateur_impact_equipement_physique ep
            inner join ref_sustainable_individual_package sip
                on sip.criteria = ep.critere
            where ep.nom_lot = :uid
            and ep.nom_organisation = :organization
            and (ep.type_equipement = 'Terminal' or ep.type_equipement = 'Network' or ep.type_equipement = 'Dedicated Server')
            group by
                ep.type_equipement,
                ep.critere,
                ep.unite
            union
            select
               'Server'                                                     as tier,
               ev.critere                                                   as criteria,
               ev.unite                                                     as unit,
               sum(ev.impact_unitaire)                                      as unit_value,
               sum(ev.impact_unitaire/sip.individual_sustainable_package)   as sip_value
            from ind_indicateur_impact_equipement_virtuel ev
            inner join ref_sustainable_individual_package sip
            on sip.criteria = ev.critere
            where ev.nom_lot = :uid
            and ev.nom_organisation = :organization
            group by
                ev.critere,
                ev.unite
        )
        select
            row_number() over() as id,
            tier,
            criteria,
            unit,
            sum(unit_value)     as unit_value,
            sum(sip_value)      as sip_value
         from indicator_virtual_and_physical_equipment group by tier, criteria, unit
         ;
    """)
@Entity
@Data
@SuperBuilder
@AllArgsConstructor
public class DigitalServiceIndicatorView implements Serializable {

    @Id
    private Long id;

    private String tier;

    private String criteria;

    private String unit;

    private Float unitValue;

    private Float sipValue;

}
