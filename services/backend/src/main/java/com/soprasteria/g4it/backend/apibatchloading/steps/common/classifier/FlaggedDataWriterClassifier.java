/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apibatchloading.steps.common.classifier;

import com.soprasteria.g4it.backend.common.dbmodel.AbstractValidationBaseEntity;
import lombok.AllArgsConstructor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.classify.Classifier;

/**
 * Classifier Bad / Good Data Writer
 */
@AllArgsConstructor
public class FlaggedDataWriterClassifier<T extends AbstractValidationBaseEntity> implements Classifier<T, ItemWriter<? super T>> {

    /**
     * Writer to write good data in database.
     */
    private transient ItemWriter<T> repositoryItemWriter;

    /**
     * Writer to write bad data in reject file.
     */
    private transient ItemWriter<T> flatFileItemWriter;

    /**
     * {@inheritDoc}
     */
    @Override
    public ItemWriter<? super T> classify(final T classifiable) {
        if (classifiable.getValid() != null && classifiable.getValid()) {
            return repositoryItemWriter;
        } else {
            return flatFileItemWriter;
        }
    }
}
