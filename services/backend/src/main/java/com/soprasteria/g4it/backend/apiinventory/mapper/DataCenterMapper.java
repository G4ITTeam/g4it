/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apiinventory.mapper;

import com.soprasteria.g4it.backend.apiinventory.modeldb.DataCenter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

/**
 * Datacenter mapper.
 */
@Mapper
public interface DataCenterMapper {

    /**
     * Mapper instance.
     */
    DataCenterMapper INSTANCE = Mappers.getMapper(DataCenterMapper.class);

    /**
     * Merge entity.
     *
     * @param target object in database to update.
     * @param source object containing updated data.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "inventory", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    void merge(@MappingTarget final DataCenter target, final DataCenter source);
}
