/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apibatchevaluation.controller;

import com.soprasteria.g4it.backend.apibatchevaluation.business.InventoryEvaluationService;
import com.soprasteria.g4it.backend.server.gen.api.InventoryEvaluationApiDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * Inventory Evaluation Job Service.
 */
@Service
public class EvaluationController implements InventoryEvaluationApiDelegate {

    /**
     * The Spring JobExplorer
     */
    @Autowired
    private InventoryEvaluationService inventoryEvaluationService;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Long> launchEvaluationBatch(final String subscriber,
                                                      final String organization,
                                                      final Long inventoryId) {
        return ResponseEntity.accepted().body(
                inventoryEvaluationService.launchEvaluationBatchJob(subscriber, organization, inventoryId)
        );
    }

}

