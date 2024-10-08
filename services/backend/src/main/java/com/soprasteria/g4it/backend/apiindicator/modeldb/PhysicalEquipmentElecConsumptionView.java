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
        name = "PhysicalEquipmentElecConsumptionIndicatorsMapping",
        classes = @ConstructorResult(
                targetClass = PhysicalEquipmentElecConsumptionView.class,
                columns = {
                        @ColumnResult(name = "id", type = Long.class),
                        @ColumnResult(name = "nom_entite"),
                        @ColumnResult(name = "type"),
                        @ColumnResult(name = "statut"),
                        @ColumnResult(name = "country"),
                        @ColumnResult(name = "elec_consumption", type = Double.class)
                }
        )
)

@NamedNativeQuery(name = "PhysicalEquipmentElecConsumptionView.findPhysicalEquipmentElecConsumptionIndicators", resultSetMapping = "PhysicalEquipmentElecConsumptionIndicatorsMapping", query = """
         select
           	row_number() over ()                              as id,
           	ind_ep.nom_entite                                 as nom_entite,
           	ind_ep.type_equipement                            as type,
           	ind_ep.statut_equipement_physique                 as statut,
           	case
           		when ep.nom_court_datacenter <> ''
           		  and ep.nom_court_datacenter is not null
           		  and dc.localisation <> ''
           		  and dc.localisation is not null
           	    then dc.localisation
           		else ep.pays_utilisation
           	end                                               as country,
           	SUM(ind_ep.conso_elec_moyenne) / :criteriaNumber  as elec_consumption
           from
           	ind_indicateur_impact_equipement_physique as ind_ep
           cross join en_equipement_physique as ep
           left join EN_DATA_CENTER dc on
           	dc.nom_lot = ep.nom_lot
           	and dc.nom_court_datacenter = ep.nom_court_datacenter
           where
           	ind_ep.statut_indicateur = 'OK'
           	and ind_ep.etapeacv = 'UTILISATION'
           	and ind_ep.nom_lot = :batchName
           	and ind_ep.nom_equipement = ep.nom_equipement_physique
           	and ep.nom_lot = :batchName
           group by
           	country,
           	ind_ep.type_equipement,
           	ind_ep.nom_entite,
           	ind_ep.statut_equipement_physique;
        """)
@Data
@Entity
@SuperBuilder
@AllArgsConstructor
public class PhysicalEquipmentElecConsumptionView implements Serializable {

    @Id
    private Long id;

    private String nomEntite;

    private String type;

    private String statut;

    private String country;

    private Double elecConsumption;
}
