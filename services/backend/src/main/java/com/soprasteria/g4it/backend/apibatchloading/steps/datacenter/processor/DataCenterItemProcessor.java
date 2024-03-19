/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apibatchloading.steps.datacenter.processor;

import com.soprasteria.g4it.backend.apiinventory.modeldb.DataCenter;
import io.micrometer.common.util.StringUtils;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.AllArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.context.MessageSource;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * DataCenter input file processor.
 */
@AllArgsConstructor
public class DataCenterItemProcessor implements ItemProcessor<DataCenter, DataCenter> {

    /**
     * Current Session Date.
     */
    private final Date sessionDate;

    /**
     * The inventory unique identifier.
     */
    private final long inventoryId;

    /**
     * Validator
     */
    private final Validator validator;

    /**
     * Validation message.
     */
    private final MessageSource messageSource;

    /**
     * Countries referential
     */
    private final List<String> countriesReferential;

    /**
     * The locale to use.
     */
    private final Locale locale;

    /**
     * {@inheritDoc}
     */
    @Override
    public DataCenter process(final DataCenter dataCenter) throws Exception {
        // Init datacenter with common information
        dataCenter.setSessionDate(sessionDate);
        dataCenter.setInventoryId(inventoryId);
        dataCenter.setInputFileName(dataCenter.getResource().getFilename());

        // surfacing control.
        dataCenter.setValid(true);
        final Set<ConstraintViolation<DataCenter>> violations = validator.validate(dataCenter);
        if (!violations.isEmpty()) {
            dataCenter.setValid(false);
            dataCenter.setMessage(violations.stream().map(ConstraintViolation::getMessage).sorted().collect(Collectors.joining(", ")));
        }

        // referential control.
        if (StringUtils.isNotBlank(dataCenter.getLocalisation()) && !countriesReferential.contains(dataCenter.getLocalisation())) {
            dataCenter.setValid(false);
            final String messageCountry = messageSource.getMessage("referential.country", new String[]{dataCenter.getLocalisation()}, locale);
            dataCenter.setMessage(Stream.of(dataCenter.getMessage(), messageCountry).filter(Objects::nonNull).collect(Collectors.joining(" ")));
        }
        return dataCenter;
    }
}
