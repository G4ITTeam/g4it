/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiindicator.repository;

import com.soprasteria.g4it.backend.apiindicator.modeldb.AggEquipmentIndicator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface AggEquipmentIndicatorRepository extends JpaRepository<AggEquipmentIndicator, Long> {

    /**
     * method to insert main indicators in aggregation table.
     *
     * @param batchName the batch name.
     */
    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = """
            INSERT INTO agg_equipment_indicator (criteria,acv_step,entity,equipment,status,status_indicator,country,batch_name,impact,unit,sip,quantity)
                     SELECT
                           iep.critere AS criteria,
                           iep.etapeacv AS acv_step,
                           iep.nom_entite AS entity,
                           iep.type_equipement AS equipment,
                           iep.statut_equipement_physique AS status,
                           iep.statut_indicateur AS status_indicator,
                           CASE
                               WHEN en_equipement_physique.nom_court_datacenter <> '' AND en_equipement_physique.nom_court_datacenter IS NOT NULL
                                   AND data_center.localisation <> '' AND data_center.localisation IS NOT NULL
                               THEN data_center.localisation
                               ELSE en_equipement_physique.pays_utilisation
                           END AS country,
                           iep.nom_lot AS batch_name,
                           SUM(iep.impact_unitaire) AS impact,
                           iep.unite AS unit,
                           SUM(iep.impact_unitaire / ref_sip.individual_sustainable_package) AS sip,
                           SUM(iep.quantite) AS quantity
                        FROM
                           ind_indicateur_impact_equipement_physique iep
                           CROSS JOIN en_equipement_physique
                           INNER JOIN REF_SUSTAINABLE_INDIVIDUAL_PACKAGE ref_sip
                               ON ref_sip.criteria = iep.critere
                           LEFT JOIN EN_DATA_CENTER data_center
                               ON data_center.nom_lot = en_equipement_physique.nom_lot
                               AND data_center.nom_court_datacenter = en_equipement_physique.nom_court_datacenter
                        WHERE
                           iep.nom_lot = :batchName
                           AND iep.nom_equipement = en_equipement_physique.nom_equipement_physique
                           AND en_equipement_physique.nom_lot = :batchName
                        GROUP BY
                           iep.critere,
                           iep.etapeacv,
                           iep.nom_entite,
                           iep.type_equipement,
                           iep.statut_equipement_physique,
                           country,
                           iep.nom_lot,
                           iep.unite,
                           iep.statut_indicateur
            """)
    void insertIntoAggEquipmentIndicators(@Param("batchName") String batchName);

    /**
     * method to recovery of main indicators.
     *
     * @param batchName the batch name.
     * @return main indicators
     */
    List<AggEquipmentIndicator> findByBatchName(final String batchName);

    @Transactional
    @Modifying
    void deleteByBatchName(final String batchName);

}