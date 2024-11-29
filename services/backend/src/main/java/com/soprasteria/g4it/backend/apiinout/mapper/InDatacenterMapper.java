/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiinout.mapper;

import com.soprasteria.g4it.backend.apiinout.modeldb.InDatacenter;
import com.soprasteria.g4it.backend.server.gen.api.dto.InDatacenterRest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * in datacenter mapper.
 */
@Mapper(componentModel = "spring")
public interface InDatacenterMapper {

    List<InDatacenterRest> toRest(final List<InDatacenter> source);

    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "lastUpdateDate", ignore = true)
    InDatacenterRest toRest(final InDatacenter source);

    @Mapping(target = "lastUpdateDate", expression = "java(java.time.LocalDateTime.now())")
    InDatacenter toEntity(final InDatacenterRest source);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "lastUpdateDate", expression = "java(java.time.LocalDateTime.now())")
    void merge(@MappingTarget final InDatacenter target, final InDatacenter source);
}
