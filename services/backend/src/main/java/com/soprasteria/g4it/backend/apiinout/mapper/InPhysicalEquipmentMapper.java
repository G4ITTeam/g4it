/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiinout.mapper;

import com.soprasteria.g4it.backend.apiinout.modeldb.InPhysicalEquipment;
import com.soprasteria.g4it.backend.server.gen.api.dto.InPhysicalEquipmentRest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * in physical equipment mapper.
 */
@Mapper(componentModel = "spring")
public interface InPhysicalEquipmentMapper {

    List<InPhysicalEquipmentRest> toRest(final List<InPhysicalEquipment> source);

    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "lastUpdateDate", ignore = true)
    InPhysicalEquipmentRest toRest(final InPhysicalEquipment source);

    @Mapping(target = "lastUpdateDate", expression = "java(java.time.LocalDateTime.now())")
    InPhysicalEquipment toEntity(final InPhysicalEquipmentRest source);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "lastUpdateDate", expression = "java(java.time.LocalDateTime.now())")
    void merge(@MappingTarget final InPhysicalEquipment target, final InPhysicalEquipment source);
}
