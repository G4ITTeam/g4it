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
                        @ColumnResult(name = "sip_value", type = Float.class),
                        @ColumnResult(name = "status"),
                        @ColumnResult(name = "count_value", type = Long.class)
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
                     sum(ep.impact_unitaire/sip.individual_sustainable_package) as sip_value,
                     ep.statut_indicateur                                       as status,
                     count(*)                                                   as count_value
                 from ind_indicateur_impact_equipement_physique ep
                 inner join ref_sustainable_individual_package sip
                     on sip.criteria = ep.critere
                 where ep.nom_lot = :uid
                 and (ep.type_equipement = 'Terminal' or ep.type_equipement = 'Network' or ep.type_equipement = 'Dedicated Server')
                 group by
                     ep.type_equipement,
                     ep.critere,
                     ep.unite,
                     ep.statut_indicateur
                 union
                 select
                    'Server'                                                     as tier,
                    ev.critere                                                   as criteria,
                    ev.unite                                                     as unit,
                    sum(ev.impact_unitaire)                                      as unit_value,
                    sum(ev.impact_unitaire/sip.individual_sustainable_package)   as sip_value,
                    ev.statut_indicateur                                         as status,
                    count(*)                                                     as count_value
                 from ind_indicateur_impact_equipement_virtuel ev
                 inner join ref_sustainable_individual_package sip
                 on sip.criteria = ev.critere
                 where ev.nom_lot = :uid
                 group by
                     ev.critere,
                     ev.unite,
                     ev.statut_indicateur
                 union
                 select
                    'CloudService'                                            as tier,
                    ove.criterion                                             as criteria,
                    ove.unit                                                  as unit,
                    sum(ove.unit_impact)                                      as unit_value,
                    sum(ove.people_eq_impact)                                 as sip_value,
                    case
                        when ove.status_indicator = 'ERROR'
                           then 'ERREUR'
                        else ove.status_indicator
                        end                                                   as status,
                    sum(ove.count_value)                                      as count_value
                    from out_virtual_equipment ove
                    where ove.task_id  = (select id from task t where t.digital_service_uid = :uid)
                    group by
                    ove.criterion,
                    ove.unit,
                    ove.status_indicator
             )
             select
                 row_number() over() as id,
                 tier,
                 criteria,
                 unit,
                 sum(unit_value)     as unit_value,
                 sum(sip_value)      as sip_value,
                 status,
                 SUM(count_value) AS count_value
             from indicator_virtual_and_physical_equipment
             group by tier, criteria, unit, status;
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

    private String status;

    private Long countValue;
}
