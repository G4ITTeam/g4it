/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apibatchevaluation.business;

import com.soprasteria.g4it.backend.apiindicator.repository.AggApplicationIndicatorRepository;
import com.soprasteria.g4it.backend.apiindicator.repository.AggEquipmentIndicatorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AggregationService {

    @Autowired
    private AggEquipmentIndicatorRepository aggEquipmentIndicatorRepository;

    @Autowired
    private AggApplicationIndicatorRepository aggApplicationIndicatorRepository;

    /**
     * Execute the aggregation of batch data
     *
     * @param batchName the batch name
     */
    public void aggregateBatchData(final String batchName) {
        aggEquipmentIndicatorRepository.deleteByBatchName(batchName);
        aggEquipmentIndicatorRepository.insertIntoAggEquipmentIndicators(batchName);
        aggApplicationIndicatorRepository.deleteByBatchName(batchName);
        aggApplicationIndicatorRepository.insertIntoAggApplicationIndicators(batchName);
    }
}
