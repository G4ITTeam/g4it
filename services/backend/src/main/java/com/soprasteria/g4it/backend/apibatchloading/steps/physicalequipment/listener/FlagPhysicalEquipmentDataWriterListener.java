/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apibatchloading.steps.physicalequipment.listener;

import com.soprasteria.g4it.backend.apiinventory.mapper.PhysicalEquipmentMapper;
import com.soprasteria.g4it.backend.apiinventory.modeldb.PhysicalEquipment;
import com.soprasteria.g4it.backend.apiinventory.repository.PhysicalEquipmentRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.listener.StepListenerSupport;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * Flag physical equipment listener.
 * Use to update when duplicate.
 */
@AllArgsConstructor
@Slf4j
public class FlagPhysicalEquipmentDataWriterListener extends StepListenerSupport<PhysicalEquipment, PhysicalEquipment> {

    /**
     * The repository to access physical equipment data.
     */
    private PhysicalEquipmentRepository physicalEquipmentRepository;

    /**
     * Physical Equipment mapper.
     */
    private PhysicalEquipmentMapper physicalEquipmentMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onSkipInWrite(final PhysicalEquipment physicalEquipment, final Throwable exception) {
        if (exception instanceof DataIntegrityViolationException) {
            log.info("Duplicate Physical Equipment {}, update values {}...", physicalEquipment.getNomEquipementPhysique(), physicalEquipment.getPaysDUtilisation());
            physicalEquipmentRepository.findByInventoryIdAndNomEquipementPhysique(physicalEquipment.getInventoryId(), physicalEquipment.getNomEquipementPhysique())
                    .ifPresentOrElse(physicalEquipmentDb -> update(physicalEquipment, physicalEquipmentDb), () -> physicalEquipmentRepository.save(physicalEquipment));
        } else {
            log.warn("Unexpected Exception... ", exception);
        }
    }

    /**
     * Method to update the physical equipment.
     *
     * @param source the physical equipment read.
     * @param target the physical equipment from database.
     */
    private void update(final PhysicalEquipment source, final PhysicalEquipment target) {
        physicalEquipmentMapper.merge(target, source);
        physicalEquipmentRepository.save(target);
    }

}
