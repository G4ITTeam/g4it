/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apidigitalservice.mapper;

import com.soprasteria.g4it.backend.apiindicator.model.DigitalServiceNetworkIndicatorBO;
import com.soprasteria.g4it.backend.server.gen.api.dto.DigitalServiceNetworkIndicatorRest;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DigitalServiceNetworkIndicatorRestMapper {

    DigitalServiceNetworkIndicatorRest toDto(final DigitalServiceNetworkIndicatorBO source);

    List<DigitalServiceNetworkIndicatorRest> toDto(final List<DigitalServiceNetworkIndicatorBO> source);
}
