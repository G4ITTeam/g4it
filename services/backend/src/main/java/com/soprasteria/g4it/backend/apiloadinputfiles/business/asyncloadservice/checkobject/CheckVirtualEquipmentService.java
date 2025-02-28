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
import com.soprasteria.g4it.backend.common.utils.InfrastructureType;
import com.soprasteria.g4it.backend.server.gen.api.dto.InVirtualEquipmentRest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
    public List<LineError> checkRules(final Context context, final InVirtualEquipmentRest virtualEquipment,
                                      final int line, Set<String> virtualEquipmentNames) {

        List<LineError> errors = new ArrayList<>();

        if (InfrastructureType.CLOUD_SERVICES.name().equals(virtualEquipment.getInfrastructureType())) {

            //  check equipment name is not empty
            ruleVirtualEquipmentService.checkVirtualEquipmentName(context.getLocale(), line,
                            virtualEquipment.getName(), virtualEquipmentNames)
                    .ifPresent(errors::add);

            //  check quantity is not empty
            ruleVirtualEquipmentService.checkCloudQuantity(context.getLocale(), line,
                            virtualEquipment.getQuantity())
                    .ifPresent(errors::add);

            // check cloud provider is not empty and is in BoaviztAPI referential
            ruleVirtualEquipmentService.checkCloudProvider(context.getLocale(), line,
                            virtualEquipment.getProvider())
                    .ifPresent(errors::add);

            // check instance type is not empty and  in BoaviztAPI referential
            ruleVirtualEquipmentService.checkCloudInstanceType(context.getLocale(), line,
                            virtualEquipment.getProvider(), virtualEquipment.getInstanceType())
                    .ifPresent(errors::add);

            // check location is not empty and is in BoaviztAPI referential
            ruleVirtualEquipmentService.checkCloudLocation(context.getLocale(), line,
                            virtualEquipment.getLocation())
                    .ifPresent(errors::add);

            // check workload is not empty
            ruleVirtualEquipmentService.checkCloudWorkload(context.getLocale(), line,
                            virtualEquipment.getWorkload())
                    .ifPresent(errors::add);

            // check annual usage duration is not empty
            ruleVirtualEquipmentService.checkCloudUsageDuration(context.getLocale(), line,
                            virtualEquipment.getDurationHour())
                    .ifPresent(errors::add);
        } else {

            ruleVirtualEquipmentService.checkPhysicalEquipmentLinked(context.getLocale(), line,
                            virtualEquipment.getInfrastructureType(), virtualEquipment.getPhysicalEquipmentName())
                    .ifPresent(errors::add);
            ruleVirtualEquipmentService.checkType(context.getLocale(), line,
                            virtualEquipment.getType())
                    .ifPresent(errors::add);

        }
        return errors;
    }
}
