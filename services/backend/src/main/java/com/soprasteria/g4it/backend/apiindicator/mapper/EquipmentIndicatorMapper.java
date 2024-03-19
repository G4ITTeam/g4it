/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apiindicator.mapper;

import com.soprasteria.g4it.backend.apiindicator.model.EquipmentImpactBO;
import com.soprasteria.g4it.backend.apiindicator.model.EquipmentIndicatorBO;
import com.soprasteria.g4it.backend.apiindicator.modeldb.EquipmentIndicatorView;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EquipmentIndicatorMapper {

    List<EquipmentImpactBO> toImpact(final List<EquipmentIndicatorView> source);

    EquipmentImpactBO toImpact(final EquipmentIndicatorView source);

    @Mapping(target = "impacts", source = "source")
    default EquipmentIndicatorBO toDto(final List<EquipmentIndicatorView> source) {
        return EquipmentIndicatorBO.builder()
                .unit(source.get(0).getUnit())
                .label(source.get(0).getCriteria())
                .impacts(toImpact(source))
                .build();
    }

}
