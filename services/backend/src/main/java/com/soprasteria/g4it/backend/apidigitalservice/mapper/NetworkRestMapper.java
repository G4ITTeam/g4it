/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apidigitalservice.mapper;

import com.soprasteria.g4it.backend.apidigitalservice.model.NetworkBO;
import com.soprasteria.g4it.backend.server.gen.api.dto.NetworkRest;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * Network Rest Mapper.
 */
@Mapper(componentModel = "spring", uses = {DateMapper.class})
public interface NetworkRestMapper {

    /**
     * Map to Data Transfert Object.
     *
     * @param source network business object.
     * @return network DTO.
     */
    NetworkRest toDto(final NetworkBO source);

    /**
     * Map to Data Transfert Objects.
     *
     * @param source network business objects.
     * @return networks DTO.
     */
    List<NetworkBO> toDto(final List<NetworkBO> source);

    /**
     * Map to Business Object.
     *
     * @param source network DTO.
     * @return network BO.
     */
    NetworkBO toBusinessObject(final NetworkRest source);

    /**
     * Map to Business Objects.
     *
     * @param source network DTOs.
     * @return network BOs.
     */
    List<NetworkBO> toBusinessObject(final List<NetworkRest> source);
}
