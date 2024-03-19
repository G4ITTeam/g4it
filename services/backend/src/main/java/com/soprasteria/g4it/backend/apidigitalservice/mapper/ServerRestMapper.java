/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apidigitalservice.mapper;

import com.soprasteria.g4it.backend.apidigitalservice.model.ServerBO;
import com.soprasteria.g4it.backend.server.gen.api.dto.ServerRest;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * DigitalServiceRest Mapper.
 */
@Mapper(componentModel = "spring", uses = {DateMapper.class, ServerDataCenterRestMapper.class, VirtualMachineRestMapper.class})
public interface ServerRestMapper {

    /**
     * Map to Data Transfer Object.
     *
     * @param source the source.
     * @return the ServerRest.
     */
    ServerRest toDto(final ServerBO source);

    /**
     * Map to Data Transfer Objects.
     *
     * @param source the source.
     * @return the ServerRest List.
     */
    List<ServerRest> toDtos(final List<ServerBO> source);

    /**
     * Map to Business Object.
     *
     * @param source the source.
     * @return the ServerBO.
     */
    ServerBO toBusinessObject(final ServerRest source);

    /**
     * Map to Business Objects.
     *
     * @param source the source.
     * @return the ServerBO List.
     */
    List<ServerBO> toBusinessObjects(final List<ServerRest> source);
}
