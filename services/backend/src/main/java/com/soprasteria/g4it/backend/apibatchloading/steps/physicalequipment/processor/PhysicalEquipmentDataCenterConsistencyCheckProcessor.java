/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apibatchloading.steps.physicalequipment.processor;

import com.soprasteria.g4it.backend.apiinventory.modeldb.PhysicalEquipment;
import lombok.AllArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.context.MessageSource;

import java.util.Locale;

/**
 * Physical Equipment - DataCenter Consistency check Processor.
 */
@AllArgsConstructor
public class PhysicalEquipmentDataCenterConsistencyCheckProcessor implements ItemProcessor<PhysicalEquipment, PhysicalEquipment> {

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
    public PhysicalEquipment process(final PhysicalEquipment physicalEquipment) throws Exception {
        physicalEquipment.setMessage(messageSource.getMessage("datacenter.should.exist", new String[]{physicalEquipment.getNomCourtDatacenter()}, locale));
        return physicalEquipment;
    }
}
