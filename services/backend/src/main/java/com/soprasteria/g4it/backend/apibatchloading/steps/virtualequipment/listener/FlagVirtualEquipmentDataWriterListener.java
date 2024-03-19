/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apibatchloading.steps.virtualequipment.listener;

import com.soprasteria.g4it.backend.apiinventory.mapper.VirtualEquipmentMapper;
import com.soprasteria.g4it.backend.apiinventory.modeldb.VirtualEquipment;
import com.soprasteria.g4it.backend.apiinventory.repository.VirtualEquipmentRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.listener.StepListenerSupport;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * Flag virtual equipment listener.
 * Use to update when duplicate.
 */
@AllArgsConstructor
@Slf4j
public class FlagVirtualEquipmentDataWriterListener extends StepListenerSupport<VirtualEquipment, VirtualEquipment> {

    /**
     * The repository to access virtual equipment data.
     */
    private VirtualEquipmentRepository virtualEquipmentRepository;

    /**
     * Virtual Equipment mapper.
     */
    private VirtualEquipmentMapper virtualEquipmentMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onSkipInWrite(final VirtualEquipment virtualEquipment, final Throwable exception) {
        if (exception instanceof DataIntegrityViolationException) {
            log.debug("Duplicate Virtual Equipment {}, update values...", virtualEquipment.getNomEquipementVirtuel());
            virtualEquipmentRepository.findByInventoryIdAndNomEquipementVirtuel(virtualEquipment.getInventoryId(), virtualEquipment.getNomEquipementVirtuel())
                    .ifPresent(virtualEquipmentDb -> update(virtualEquipment, virtualEquipmentDb));
        } else {
            log.warn("Unexpected Exception... ", exception);
        }
    }

    /**
     * Method to update the virtual equipment.
     *
     * @param source the virtual equipment read.
     * @param target the virtual equipment from database.
     */
    private void update(final VirtualEquipment source, final VirtualEquipment target) {
        virtualEquipmentMapper.merge(target, source);
        virtualEquipmentRepository.save(target);
    }

}
