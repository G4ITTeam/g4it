/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.checkobject;

import com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.rules.GenericRuleService;
import com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.rules.RuleVirtualEquipmentService;
import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.model.LineError;
import com.soprasteria.g4it.backend.server.gen.api.dto.InVirtualEquipmentRest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CheckVirtualEquipmentService {

    @Autowired
    GenericRuleService genericRuleService;

    @Autowired
    RuleVirtualEquipmentService ruleVirtualEquipmentService;

    /**
     * Check a virtual equipment object
     *
     * @param context          the context
     * @param virtualEquipment the virtual equipment
     * @param line             the line number
     * @return the list of errors
     */
    public List<LineError> checkRules(final Context context, final InVirtualEquipmentRest virtualEquipment, final int line) {
        List<LineError> errors = new ArrayList<>();

        // check InPhysicalEquipmentRest constraint violations
        genericRuleService.checkViolations(virtualEquipment, line)
                .ifPresent(errors::add);

        ruleVirtualEquipmentService.checkPhysicalEquipmentLinked(context.getLocale(), line, virtualEquipment.getInfrastructureType(), virtualEquipment.getPhysicalEquipmentName())
                .ifPresent(errors::add);

        return errors;
    }
}
