/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apibatchloading.steps.virtualequipment.processor;

import com.soprasteria.g4it.backend.apiinventory.modeldb.VirtualEquipment;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.AllArgsConstructor;
import org.springframework.batch.item.ItemProcessor;

import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * DataCenter input file processor.
 */
@AllArgsConstructor
public class VirtualEquipmentItemProcessor implements ItemProcessor<VirtualEquipment, VirtualEquipment> {

    /**
     * Current Session Date.
     */
    private final Date sessionDate;

    /**
     * inventory id
     */
    private long inventoryId;

    /**
     * Validator
     */
    private final Validator validator;

    /**
     * {@inheritDoc}
     */
    @Override
    public VirtualEquipment process(final VirtualEquipment virtualEquipment) throws Exception {
        // set common properties
        virtualEquipment.setSessionDate(sessionDate);
        virtualEquipment.setInventoryId(inventoryId);
        virtualEquipment.setInputFileName(virtualEquipment.getResource().getFilename());

        // validate entity
        final Set<ConstraintViolation<VirtualEquipment>> violations = validator.validate(virtualEquipment);
        if (violations.isEmpty()) {
            virtualEquipment.setValid(true);
        } else {
            virtualEquipment.setValid(false);
            virtualEquipment.setMessage(violations.stream().map(ConstraintViolation::getMessage).sorted().collect(Collectors.joining(", ")));
            return virtualEquipment;
        }

        // else we continue with the current one
        return virtualEquipment;
    }
}
