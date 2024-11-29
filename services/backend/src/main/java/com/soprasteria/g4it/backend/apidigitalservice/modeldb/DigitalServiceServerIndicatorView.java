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
        name = "DigitalServiceServerIndicatorMapping",
        classes = @ConstructorResult(
                targetClass = DigitalServiceServerIndicatorView.class,
                columns = {
                        @ColumnResult(name = "id", type = Long.class),
                        @ColumnResult(name = "criteria"),
                        @ColumnResult(name = "mutualization_type"),
                        @ColumnResult(name = "type"),
                        @ColumnResult(name = "server_name"),
                        @ColumnResult(name = "vm_name"),
                        @ColumnResult(name = "vm_uid"),
                        @ColumnResult(name = "quantity", type = Integer.class),
                        @ColumnResult(name = "lifecycle_step"),
                        @ColumnResult(name = "pue", type = Double.class),
                        @ColumnResult(name = "country"),
                        @ColumnResult(name = "raw_value", type = Double.class),
                        @ColumnResult(name = "sip_value", type = Double.class),
                        @ColumnResult(name = "unit"),
                        @ColumnResult(name = "status"),
                        @ColumnResult(name = "count_value", type = Long.class)
                }
        )
)
@NamedNativeQuery(
        name = "DigitalServiceServerIndicatorView.findDigitalServiceServerIndicators",
        resultSetMapping = "DigitalServiceServerIndicatorMapping",
        query = """
                select
                    row_number() over ()                                            as id,
                    *
                from (
                    select
                        ep.critere                                                  as criteria,
                        s.mutualization_type                                        as mutualization_type,
                        s.type                                                      as type,
                        s.name                                                      as server_name,
                        null                                                        as vm_name,
                        null                                                        as vm_uid,
                        null                                                        as quantity,
                        ep.etapeacv                                                 as lifecycle_step,
                        dc.pue                                                      as pue,
                        dc.location                                                 as country,
                        sum(ep.impact_unitaire)                                     as raw_value,
                        sum(ep.impact_unitaire/sip.individual_sustainable_package)  as sip_value,
                        ep.unite                                                    as unit,
                        ep.statut_indicateur                                        as status,
                        count(*)                                                    as count_value
                    from
                        ind_indicateur_impact_equipement_physique ep
                    inner join server s
                        on s.uid = ep.nom_equipement
                    inner join datacenter_digital_service dc
                        on s.digital_service_uid = dc.digital_service_uid
                    inner join ref_sustainable_individual_package sip
                        on sip.criteria = ep.critere
                    where ep.nom_lot = :uid
                    and ep.type_equipement = 'Dedicated Server'
                    group by
                        ep.critere,
                        s.mutualization_type,
                        s.type,
                        s.name,
                        ep.etapeacv,
                        dc.pue,
                        dc.location,
                        ep.unite,
                        ep.statut_indicateur
                    union
                    select
                        ev.critere                                                  as criteria,
                        s.mutualization_type                                        as mutualization_type,
                        s.type                                                      as type,
                        s.name                                                      as server_name,
                        vm.name                                                     as vm_name,
                        vm.uid                                                      as vm_uid,
                        avg(vm.quantity)                                            as quantity,
                        ev.etapeacv                                                 as lifecycle_step,
                        dc.pue                                                      as pue,
                        dc.location                                                 as country,
                        sum(ev.impact_unitaire)                                     as raw_value,
                        sum(ev.impact_unitaire/sip.individual_sustainable_package)  as sip_value,
                        ev.unite                                                    as unit,
                        ev.statut_indicateur                                        as status,
                        count(*)                                                    as count_value
                    from ind_indicateur_impact_equipement_virtuel ev
                    inner join virtual_equipment_digital_service vm
                        on vm.uid = ev.nom_equipement_virtuel
                    inner join server s
                        on s.uid = vm.server_uid
                    inner join datacenter_digital_service dc
                        on s.digital_service_uid = dc.digital_service_uid
                    inner join ref_sustainable_individual_package sip
                        on sip.criteria = ev.critere
                    where ev.nom_lot = :uid
                    group by
                        ev.critere,
                        s.mutualization_type,
                        s.type,
                        s.name,
                        vm.name,
                        vm.uid,
                        ev.etapeacv,
                        dc.pue,
                        dc.location,
                        ev.unite,
                        ev.statut_indicateur
                ) as servers
                """
)
@Entity
@Data
@SuperBuilder
@AllArgsConstructor
public class DigitalServiceServerIndicatorView implements Serializable {

    @Id
    private Long id;

    private String criteria;

    private String mutualizationType;

    private String type;

    private String serverName;

    private String vmName;

    private String vmUid;

    private Integer quantity;

    private String lifecycleStep;

    private Double pue;

    private String country;

    private Double rawValue;

    private Double sipValue;

    private String unit;

    private String status;

    private Long countValue;
}
