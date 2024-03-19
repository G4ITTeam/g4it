/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apidigitalservice.mapper;

import com.soprasteria.g4it.backend.apiindicator.model.DigitalServiceIndicatorBO;
import com.soprasteria.g4it.backend.server.gen.api.dto.DigitalServiceIndicatorRest;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * Digital Service Indicator Mapper.
 */
@Mapper(componentModel = "spring")
public interface DigitalServiceIndicatorRestMapper {

    /**
     * Map do dto.
     *
     * @param source business object.
     * @return the dto.
     */
    DigitalServiceIndicatorRest toDto(final DigitalServiceIndicatorBO source);

    /**
     * Map to dto list.
     *
     * @param source business object list.
     * @return the dto list.
     */
    List<DigitalServiceIndicatorRest> toDto(final List<DigitalServiceIndicatorBO> source);

}
