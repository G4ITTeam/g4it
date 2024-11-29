/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.common.evaluating.mapper;

import com.soprasteria.g4it.backend.apiinout.modeldb.InVirtualEquipment;
import com.soprasteria.g4it.backend.apiinout.modeldb.OutVirtualEquipment;
import com.soprasteria.g4it.backend.common.evaluating.model.AggValuesBO;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.external.boavizta.business.BoaviztapiService;
import org.mapstruct.Mapper;

import java.util.Map;

@Mapper(componentModel = "spring")
public interface AggregationToOut {

    default String keyVirtualEquipment(InVirtualEquipment virtualEquipment, String criterion, String lifecycleStep, String statusIndicator, String unit) {
        return String.join("|",
                criterion,
                lifecycleStep,
                virtualEquipment.getName(),
                virtualEquipment.getLocation(),
                statusIndicator,
                unit,
                virtualEquipment.getInfrastructureType(),
                virtualEquipment.getInstanceType(),
                virtualEquipment.getProvider()
        );
    }

    default OutVirtualEquipment mapVirtualEquipment(String key, AggValuesBO agg, Long taskId, Map<String, Double> refSip) {
        String[] values = key.split("\\|");

        return OutVirtualEquipment.builder()
                .taskId(taskId)
                .type(Constants.CLOUD_TYPE)
                .criterion(values[0])
                .lifecycleStep(values[1])
                .name(values[2])
                .location(values[3])
                .statusIndicator(values[4])
                .unit(values[5])
                .infrastructureType(values[6])
                .instanceType(values[7])
                .provider(values[8])
                .quantity(agg.getQuantity())
                .electricityConsumption(agg.getElectricityConsumption())
                .unitImpact(agg.getUnitImpact())
                .peopleEqImpact(agg.getUnitImpact() / refSip.get(values[0]))
                .countValue(agg.getCountValue())
                .usageDuration(agg.getUsageDuration())
                .workload(agg.getQuantity() == 0 ? 0 : agg.getWorkload() / agg.getQuantity())
                .errors(agg.getErrors().isEmpty() ? null : agg.getErrors())
                .engineName(BoaviztapiService.BOAVIZTAPI_ENGINE)
                .engineVersion(BoaviztapiService.BOAVIZTAPI_VERSION)
                .referentialVersion(Constants.REFERENTIAL_VERSION_CLOUD)
                .build();
    }
}
