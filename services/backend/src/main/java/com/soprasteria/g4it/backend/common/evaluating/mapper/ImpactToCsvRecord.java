/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.common.evaluating.mapper;

import com.soprasteria.g4it.backend.apiinout.modeldb.InVirtualEquipment;
import com.soprasteria.g4it.backend.common.evaluating.model.AggValuesBO;
import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.external.boavizta.business.BoaviztapiService;
import org.mapstruct.Mapper;

import java.util.List;

import static com.soprasteria.g4it.backend.common.utils.CsvUtils.print;

@Mapper(componentModel = "spring")
public interface ImpactToCsvRecord {

    default List<String> toCsv(Context context, Long taskId, InVirtualEquipment virtualEquipment,
                               AggValuesBO values, String criterion, String lifecycleStep, String indicatorStatus, String unit) {

        return List.of(
                context.getDigitalServiceName(),
                context.getDatetime().format(Constants.LOCAL_DATE_TIME_FORMATTER_MS),
                context.getOrganizationId().toString(),
                taskId.toString(),
                lifecycleStep,
                criterion,
                "",
                virtualEquipment.getName(),
                virtualEquipment.getInstanceType(),
                virtualEquipment.getProvider(),
                virtualEquipment.getLocation(),
                BoaviztapiService.BOAVIZTAPI_ENGINE,
                BoaviztapiService.BOAVIZTAPI_VERSION,
                Constants.REFERENTIAL_VERSION_CLOUD,
                indicatorStatus,
                print(unit),
                print(values.getUnitImpact()),
                print(values.getPeopleEqImpact()),
                print(values.getQuantity()),
                values.getErrors() == null ? "" : String.join(", ", values.getErrors())
        );

    }
}
