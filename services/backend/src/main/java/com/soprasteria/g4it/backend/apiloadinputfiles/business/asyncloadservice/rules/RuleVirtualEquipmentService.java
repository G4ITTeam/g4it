/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.rules;

import com.soprasteria.g4it.backend.common.model.LineError;
import com.soprasteria.g4it.backend.common.utils.InfrastructureType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;

@Service
public class RuleVirtualEquipmentService {

    @Autowired
    MessageSource messageSource;

    public Optional<LineError> checkPhysicalEquipmentLinked(Locale locale, int line, final String infrastructureType, final String physicalEquipmentName) {

        if (!InfrastructureType.CLOUD_SERVICES.toString().equals(infrastructureType) && physicalEquipmentName == null) {
            return Optional.of(new LineError(line, messageSource.getMessage(
                    "virtual.equipment.must.have.physical.equipment",
                    new String[]{},
                    locale)));
        }

        return Optional.empty();
    }
}
