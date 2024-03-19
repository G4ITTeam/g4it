/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apibatchloading.steps.common.mapper;

import com.soprasteria.g4it.backend.common.dbmodel.AbstractValidationBaseEntity;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;

/**
 * Csv line mapper to map line with line number.
 */
public class CsvLineMapper<T extends AbstractValidationBaseEntity> extends DefaultLineMapper<T> {

    /**
     * {@inheritDoc}
     */
    @Override
    public T mapLine(final String line, final int lineNumber) throws Exception {
        final T target = super.mapLine(line, lineNumber);
        target.setLineNumber(lineNumber);
        return target;
    }
}
