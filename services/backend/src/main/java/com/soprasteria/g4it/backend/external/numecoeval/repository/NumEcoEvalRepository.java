/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.external.numecoeval.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.soprasteria.g4it.backend.apiindicator.utils.Constants.PARAM_BATCH_NAME;
import static com.soprasteria.g4it.backend.apiindicator.utils.Constants.PARAM_ORGANIZATION;

@Repository
@AllArgsConstructor
public class NumEcoEvalRepository {

    /**
     * List of NumEcoEval input tables
     */
    public static final List<String> EN_TABLES = List.of(
            "en_application",
            "en_data_center",
            "en_donnees_entrees",
            "en_entite",
            "en_equipement_physique",
            "en_equipement_virtuel",
            "en_messagerie"
    );

    /**
     * List of NumEcoEval indicator tables
     */
    public static final List<String> IND_TABLES = List.of(
            "ind_indicateur_impact_messagerie",
            "ind_indicateur_impact_application",
            "ind_indicateur_impact_reseau",
            "ind_indicateur_impact_equipement_virtuel",
            "ind_indicateur_impact_equipement_physique"
    );

    /**
     * The jakarta EntityManager
     */
    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Delete data from the table, filtered by organization and batchName
     * table parameter is forced to be one of the constant values in EN_TABLES and IND_TABLES
     *
     * @param table        the table
     * @param organization the organization.
     * @param batchName    the batch name
     */
    @Transactional
    public void deleteByOrganizationAndBatchName(final String table, final String organization, final String batchName) {
        if (EN_TABLES.contains(table) || IND_TABLES.contains(table)) {
            final Query query = entityManager.createNativeQuery(
                    String.format("delete from %s where nom_organisation = :organization and nom_lot =:batchName", table));
            query.setParameter(PARAM_ORGANIZATION, organization);
            query.setParameter(PARAM_BATCH_NAME, batchName);
            query.executeUpdate();
        }
    }
}
