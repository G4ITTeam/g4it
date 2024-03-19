/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apidigitalservice.mapper;

import com.soprasteria.g4it.backend.apidigitalservice.model.ServerDataCenterBO;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DatacenterDigitalService;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * Datacenter DigitalService Mapper.
 */
@Mapper(componentModel = "spring")
public interface DatacenterDigitalServiceMapper {

    /**
     * Map to Business Object.
     *
     * @param source the source.
     * @return the ServerDataCenterBO list.
     */
    List<ServerDataCenterBO> toBusinessObject(final List<DatacenterDigitalService> source);

    /**
     * Map to Business Objects.
     *
     * @param source the source.
     * @return the ServerDataCenterBO.
     */
    ServerDataCenterBO toBusinessObject(final DatacenterDigitalService source);

    /**
     * Map to entity.
     *
     * @param source the business object.
     * @return the entity.
     */
    DatacenterDigitalService toEntity(final ServerDataCenterBO source);
}
