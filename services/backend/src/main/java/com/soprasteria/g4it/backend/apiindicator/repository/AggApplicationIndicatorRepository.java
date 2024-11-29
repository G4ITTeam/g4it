/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiindicator.repository;

import com.soprasteria.g4it.backend.apiindicator.modeldb.AggApplicationIndicator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface AggApplicationIndicatorRepository extends JpaRepository<AggApplicationIndicator, Long> {

    /**
     * method to insert main indicators in aggregation table.
     *
     * @param batchName the batch name.
     */
    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = """
              INSERT INTO agg_application_indicator (criteria, life_cycle, domain, sub_domain, environment, equipment_type, application_name, batch_name, impact, unit, virtual_equipment_name, cluster, sip, status_indicator)
              SELECT
                 ia.critere AS criteria,
                 ia.etapeacv AS life_cycle,
                 COALESCE(ia.domaine, '') AS domain,
                 COALESCE(ia.sous_domaine, '') AS sub_domain,
                 ia.type_environnement AS environment,
                 ep.type AS equipment_type,
                 ia.nom_application AS application_name,
                 ia.nom_lot AS batch_name,
                 SUM(ia.impact_unitaire) AS impact,
                 ia.unite AS unit,
                 ia.nom_equipement_virtuel as virtual_equipment_name,
                 ev.cluster as cluster,
                 SUM(ia.impact_unitaire / ref_sip.individual_sustainable_package) AS sip,
                 ia.statut_indicateur as status_indicator
              FROM
                 ind_indicateur_impact_application ia
                 INNER JOIN ref_sustainable_individual_package ref_sip ON ref_sip.criteria = ia.critere
                 INNER JOIN en_equipement_physique ep ON ep.nom_equipement_physique  = ia.nom_equipement_physique
                 INNER JOIN en_equipement_virtuel ev ON ev.nom_equipement_virtuel = ia.nom_equipement_virtuel
              WHERE               
                 ia.nom_lot = :batchName
                 AND ep.nom_lot = :batchName
                 AND ev.nom_lot = :batchName
                 GROUP BY
                 ia.critere,
                 ia.etapeacv,
                 ia.domaine,
                 ia.sous_domaine,
                 ia.type_environnement,
                 ep.type,
                 ia.nom_application,
                 ia.nom_lot,
                 ia.unite,
                 ia.nom_equipement_virtuel,
                 ev.cluster,
                 ia.statut_indicateur
            """)
    void insertIntoAggApplicationIndicators(@Param("batchName") String batchName);

    /**
     * method to recovery of main indicators.
     *
     * @param batchName the batch name.
     * @return main indicators
     */
    List<AggApplicationIndicator> findByBatchName(final String batchName);

    @Transactional
    @Modifying
    void deleteByBatchName(final String batchName);
}
