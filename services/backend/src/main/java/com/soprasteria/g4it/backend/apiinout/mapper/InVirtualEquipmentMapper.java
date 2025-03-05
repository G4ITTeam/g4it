/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiinout.mapper;

import com.soprasteria.g4it.backend.apiinout.modeldb.InVirtualEquipment;
import com.soprasteria.g4it.backend.server.gen.api.dto.InVirtualEquipmentRest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;
import java.util.Map;

/**
 * in virtual equipment mapper.
 */
@Mapper(componentModel = "spring")
public interface InVirtualEquipmentMapper {

    List<InVirtualEquipmentRest> toRest(final List<InVirtualEquipment> source);

    InVirtualEquipmentRest toRest(final InVirtualEquipment source);

    @Mapping(target = "lastUpdateDate", expression = "java(java.time.LocalDateTime.now())")
    InVirtualEquipment toEntity(final InVirtualEquipmentRest source);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "lastUpdateDate", expression = "java(java.time.LocalDateTime.now())")
    void merge(@MappingTarget final InVirtualEquipment target, final InVirtualEquipment source);
}
