/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apibatchloading.steps.physicalequipment.processor;

import com.soprasteria.g4it.backend.apiinventory.modeldb.PhysicalEquipment;
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
public class PhysicalEquipmentItemProcessor implements ItemProcessor<PhysicalEquipment, PhysicalEquipment> {

    /**
     * Current Session Date.
     */
    private final Date sessionDate;

    /**
     * Inventory Id
     */
    private final long inventoryId;

    /**
     * Validator
     */
    private final Validator validator;

    /**
     * Internationalization message.
     */
    private final MessageSource messageSource;

    /**
     * Countries referential
     */
    private List<String> countriesReferential;

    /**
     * type referential
     */
    private List<String> typeReferential;

    /**
     * The locale to use.
     */
    private final Locale locale;

    /**
     * {@inheritDoc}
     */
    @Override
    public PhysicalEquipment process(final PhysicalEquipment physicalEquipment) throws Exception {
        // set common properties
        physicalEquipment.setSessionDate(sessionDate);
        physicalEquipment.setInventoryId(inventoryId);
        physicalEquipment.setInputFileName(physicalEquipment.getResource().getFilename());

        // validate entity
        final Set<ConstraintViolation<PhysicalEquipment>> violations = validator.validate(physicalEquipment);
        physicalEquipment.setValid(true);
        if (!violations.isEmpty()) {
            physicalEquipment.setValid(false);
            physicalEquipment.setMessage(violations.stream().map(ConstraintViolation::getMessage).sorted().collect(Collectors.joining(", ")));
        }

        // referential control.
        if (StringUtils.isNotBlank(physicalEquipment.getPaysDUtilisation()) && !countriesReferential.contains(physicalEquipment.getPaysDUtilisation())) {
            physicalEquipment.setValid(false);
            final String messageCountry = messageSource.getMessage("referential.country", new String[]{physicalEquipment.getPaysDUtilisation()}, locale);
            physicalEquipment.setMessage(Stream.of(physicalEquipment.getMessage(), messageCountry).filter(Objects::nonNull).collect(Collectors.joining(" ")));
        }
        if (StringUtils.isNotBlank(physicalEquipment.getType()) && !typeReferential.contains((physicalEquipment.getType()))) {
            physicalEquipment.setValid(false);
            final String equipmentType = messageSource.getMessage("referential.equipmenttype", new String[]{physicalEquipment.getType()}, locale);
            physicalEquipment.setMessage(Stream.of(physicalEquipment.getMessage(), equipmentType).filter(Objects::nonNull).collect(Collectors.joining(" ")));
        }

        // else we continue with the current one
        return physicalEquipment;
    }
}
