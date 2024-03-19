/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apidigitalservice.mapper;

import com.soprasteria.g4it.backend.apidigitalservice.model.ServerDataCenterBO;
import com.soprasteria.g4it.backend.server.gen.api.dto.ServerDatacenterRest;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * Digital Server DataCenter Rest Mapper.
 */
@Mapper(componentModel = "spring")
public interface ServerDataCenterRestMapper {

    /**
     * Map to data transfert object.
     *
     * @param source the business object.
     * @return the data transfer object.
     */
    ServerDatacenterRest toDto(final ServerDataCenterBO source);

    /**
     * Map to data transfert objects.
     *
     * @param source the business objects.
     * @return the data transfer objects.
     */
    List<ServerDatacenterRest> toDto(final List<ServerDataCenterBO> source);

    /**
     * Map to business object.
     *
     * @param source the data transfer object.
     * @return the business object.
     */
    ServerDataCenterBO toBusinessObject(final ServerDatacenterRest source);

}
