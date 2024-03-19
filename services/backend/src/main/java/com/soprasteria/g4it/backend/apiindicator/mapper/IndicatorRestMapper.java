/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apiindicator.mapper;

import com.soprasteria.g4it.backend.apiindicator.model.*;
import com.soprasteria.g4it.backend.server.gen.api.dto.*;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface IndicatorRestMapper {

    EquipmentIndicatorRest toDto(final EquipmentIndicatorBO source);

    List<EquipmentIndicatorRest> toDto(final List<EquipmentIndicatorBO> source);

    ApplicationIndicatorRest toApplicationIndicatorDto(final ApplicationIndicatorBO<ApplicationImpactBO> source);

    List<ApplicationIndicatorRest> toApplicationIndicatorDto(final List<ApplicationIndicatorBO<ApplicationImpactBO>> source);

    ApplicationVmIndicatorRest toApplicationVmIndicatorDto(final ApplicationIndicatorBO<ApplicationVmImpactBO> source);

    List<ApplicationVmIndicatorRest> toApplicationVmIndicatorDto(final List<ApplicationIndicatorBO<ApplicationVmImpactBO>> source);

    List<PhysicalEquipmentLowCarbonRest> toLowCarbonDto(final List<PhysicalEquipmentLowCarbonBO> source);

    PhysicalEquipmentLowCarbonRest toLowCarbonDto(final PhysicalEquipmentLowCarbonBO source);

    List<PhysicalEquipmentsAvgAgeRest> toAvgAgeDto(final List<PhysicalEquipmentsAvgAgeBO> source);

    PhysicalEquipmentsAvgAgeRest toAvgAgeDto(final PhysicalEquipmentsAvgAgeBO source);

    List<DataCentersInformationRest> toDataCenterDto(final List<DataCentersInformationBO> source);

    DataCentersInformationRest toDataCenterDto(final DataCentersInformationBO source);

    List<EquipmentFiltersRest> toEquipmentFiltersDto(final List<EquipmentFiltersBO> source);

    EquipmentFiltersRest toEquipmentFiltersDto(final EquipmentFiltersBO source);

    List<ApplicationFiltersRest> toApplicationFiltersDto(final List<ApplicationFiltersBO> source);

    ApplicationFiltersRest toApplicationFiltersDto(final ApplicationFiltersBO source);
}
