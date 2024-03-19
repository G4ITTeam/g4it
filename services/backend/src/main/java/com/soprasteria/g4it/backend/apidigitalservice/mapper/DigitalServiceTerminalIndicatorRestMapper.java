/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apidigitalservice.mapper;

import com.soprasteria.g4it.backend.apiindicator.model.DigitalServiceTerminalIndicatorBO;
import com.soprasteria.g4it.backend.server.gen.api.dto.DigitalServiceTerminalIndicatorRest;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DigitalServiceTerminalIndicatorRestMapper {

    DigitalServiceTerminalIndicatorRest toDto(final DigitalServiceTerminalIndicatorBO source);

    List<DigitalServiceTerminalIndicatorRest> toDto(final List<DigitalServiceTerminalIndicatorBO> source);
}
