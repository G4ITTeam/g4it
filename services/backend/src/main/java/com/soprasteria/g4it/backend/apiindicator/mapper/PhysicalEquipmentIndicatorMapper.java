/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiindicator.mapper;

import com.soprasteria.g4it.backend.apiindicator.model.PhysicalEquipmentElecConsumptionBO;
import com.soprasteria.g4it.backend.apiindicator.model.PhysicalEquipmentLowImpactBO;
import com.soprasteria.g4it.backend.apiindicator.model.PhysicalEquipmentsAvgAgeBO;
import com.soprasteria.g4it.backend.apiindicator.modeldb.InPhysicalEquipmentAvgAgeView;
import com.soprasteria.g4it.backend.apiindicator.modeldb.InPhysicalEquipmentElecConsumptionView;
import com.soprasteria.g4it.backend.apiindicator.modeldb.InPhysicalEquipmentLowImpactView;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PhysicalEquipmentIndicatorMapper {

    List<PhysicalEquipmentsAvgAgeBO> inPhysicalEquipmentAvgAgetoDto(final List<InPhysicalEquipmentAvgAgeView> source);

    List<PhysicalEquipmentLowImpactBO> inPhysicalEquipmentLowImpacttoDTO(final List<InPhysicalEquipmentLowImpactView> source);

    List<PhysicalEquipmentElecConsumptionBO> inPhysicalEquipmentElecConsumptionToDto(final List<InPhysicalEquipmentElecConsumptionView> source);
}
