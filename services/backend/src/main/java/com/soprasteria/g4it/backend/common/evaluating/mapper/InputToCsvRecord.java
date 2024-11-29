/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.common.evaluating.mapper;

import com.soprasteria.g4it.backend.apiinout.modeldb.InVirtualEquipment;
import org.mapstruct.Mapper;

import java.util.List;

import static com.soprasteria.g4it.backend.common.utils.CsvUtils.print;

@Mapper(componentModel = "spring")
public interface InputToCsvRecord {

    default List<String> toCsv(String digitalServiceName, InVirtualEquipment virtualEquipment) {
        return List.of(
                digitalServiceName,
                virtualEquipment.getName(),
                virtualEquipment.getProvider(),
                virtualEquipment.getInstanceType(),
                print(virtualEquipment.getQuantity()),
                virtualEquipment.getLocation(),
                print(virtualEquipment.getDurationHour()),
                print(virtualEquipment.getWorkload())
        );
    }

}
