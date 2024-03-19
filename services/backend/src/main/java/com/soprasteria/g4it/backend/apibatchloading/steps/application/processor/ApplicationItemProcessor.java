/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apibatchloading.steps.application.processor;

import com.soprasteria.g4it.backend.apiinventory.modeldb.Application;
import com.soprasteria.g4it.backend.apiinventory.modeldb.PhysicalEquipment;
import com.soprasteria.g4it.backend.apiinventory.repository.VirtualEquipmentRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.AllArgsConstructor;
import org.springframework.batch.item.ItemProcessor;

import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Application input file processor.
 */
@AllArgsConstructor
public class ApplicationItemProcessor implements ItemProcessor<Application, Application> {

    /**
     * Repository for virtual equipment.
     */
    private final VirtualEquipmentRepository virtualEquipmentRepository;

    /**
     * Current Session Date.
     */
    private final Date sessionDate;

    private final long inventoryId;

    /**
     * Validator
     */
    private final Validator validator;

    /**
     * {@inheritDoc}
     */
    @Override
    public Application process(final Application application) throws Exception {
        // set common properties
        application.setSessionDate(sessionDate);
        application.setInventoryId(inventoryId);
        application.setInputFileName(application.getResource().getFilename());

        // Surface Control
        final Set<ConstraintViolation<Application>> violations = validator.validate(application);
        if (violations.isEmpty()) {
            application.setValid(true);
        } else {
            application.setValid(false);
            application.setMessage(violations.stream().map(ConstraintViolation::getMessage).sorted().collect(Collectors.joining(", ")));
            return application;
        }

        // Get physical equipment.
        virtualEquipmentRepository.findByInventoryIdAndNomEquipementVirtuel(application.getInventoryId(), application.getNomEquipementVirtuel())
                .ifPresent(virtualEquipment -> application.setNomEquipementPhysique(Optional.ofNullable(virtualEquipment.getPhysicalEquipment()).map(PhysicalEquipment::getNomEquipementPhysique).orElse(null)));

        return application;
    }
}
