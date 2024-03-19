/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apibatchloading.steps.virtualequipment.processor;

import com.soprasteria.g4it.backend.apiinventory.modeldb.VirtualEquipment;
import lombok.AllArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.context.MessageSource;

import java.util.Locale;

/**
 * Virtual Equipement - Physical Equipment Consistency check Processor.
 */
@AllArgsConstructor
public class VirtualEquipmentPhysicalEquipmentConsistencyCheckProcessor implements ItemProcessor<VirtualEquipment, VirtualEquipment> {

    /**
     * Internationalization message.
     */
    private final MessageSource messageSource;

    /**
     * The locale to use.
     */
    private final Locale locale;

    /**
     * {@inheritDoc}
     */
    @Override
    public VirtualEquipment process(final VirtualEquipment virtualEquipment) throws Exception {
        virtualEquipment.setMessage(messageSource.getMessage("equipementphysique.should.exist", new String[]{virtualEquipment.getNomEquipementPhysique()}, locale));
        return virtualEquipment;
    }
}
