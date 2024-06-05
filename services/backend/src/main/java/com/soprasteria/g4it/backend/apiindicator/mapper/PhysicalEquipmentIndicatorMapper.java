/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiindicator.mapper;

import com.soprasteria.g4it.backend.apiindicator.model.PhysicalEquipmentLowImpactBO;
import com.soprasteria.g4it.backend.apiindicator.model.PhysicalEquipmentsAvgAgeBO;
import com.soprasteria.g4it.backend.apiindicator.modeldb.PhysicalEquipmentAvgAgeView;
import com.soprasteria.g4it.backend.apiindicator.modeldb.PhysicalEquipmentLowImpactView;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PhysicalEquipmentIndicatorMapper {

    List<PhysicalEquipmentsAvgAgeBO> physicalEquipmentAvgAgetoDto(final List<PhysicalEquipmentAvgAgeView> source);

    List<PhysicalEquipmentLowImpactBO> physicalEquipmentLowImpacttoDTO(final List<PhysicalEquipmentLowImpactView> source);
}
