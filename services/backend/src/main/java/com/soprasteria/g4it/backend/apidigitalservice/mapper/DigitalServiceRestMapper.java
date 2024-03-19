/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apidigitalservice.mapper;

import com.soprasteria.g4it.backend.apidigitalservice.model.DigitalServiceBO;
import com.soprasteria.g4it.backend.server.gen.api.dto.DigitalServiceRest;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * DigitalServiceRest Mapper.
 */
@Mapper(componentModel = "spring",
        uses = {DateMapper.class, TerminalRestMapper.class, NetworkRestMapper.class, ServerRestMapper.class})
public interface DigitalServiceRestMapper {

    /**
     * Map to Data Transfer Object.
     *
     * @param businessObject the source.
     * @return the DigitalServiceRest.
     */
    DigitalServiceRest toDto(final DigitalServiceBO businessObject);

    /**
     * Map to Data Transfer Object list.
     *
     * @param businessObjects the source list.
     * @return the Data Transfer Object list.
     */
    List<DigitalServiceRest> toDto(final List<DigitalServiceBO> businessObjects);

    /**
     * Map to Business Object.
     *
     * @param dto the Data Transfer Object.
     * @return the Business Object.
     */
    DigitalServiceBO toBusinessObject(final DigitalServiceRest dto);

}
