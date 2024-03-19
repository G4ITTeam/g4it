/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apibatchexport.processor;

import com.soprasteria.g4it.backend.apiindicator.modeldb.numecoeval.CommonIndicatorExport;
import lombok.AllArgsConstructor;
import org.springframework.batch.item.ItemProcessor;

import java.util.Map;
import java.util.Optional;

/**
 * Common Indicator processor.
 *
 * @param <T> the indicator class.
 */
@AllArgsConstructor
public class IndicatorProcessor<T extends CommonIndicatorExport> implements ItemProcessor<T, T> {

    private String inventoryName;

    private Map<String, Double> individualSustainablePackage;

    /**
     * {@inheritDoc}
     */
    @Override
    public T process(final T item) throws Exception {
        item.setInventoryName(inventoryName);
        if (item.getUnitImpact() != null && item.getCriteria() != null) {
            Optional.ofNullable(individualSustainablePackage.get(item.getCriteria())).ifPresent(sip -> item.setSipImpact(item.getUnitImpact() / individualSustainablePackage.get(item.getCriteria())));
        }
        return item;
    }
}
