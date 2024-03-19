/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apidigitalservice.mapper;

import com.soprasteria.g4it.backend.apidigitalservice.model.TerminalBO;
import com.soprasteria.g4it.backend.server.gen.api.dto.TerminalRest;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * Terminal Rest Mapper.
 */
@Mapper(componentModel = "spring", uses = {DateMapper.class})
public interface TerminalRestMapper {

    /**
     * Map to Data Transfert Object.
     *
     * @param source terminal business object.
     * @return terminal DTO.
     */
    TerminalRest toDto(final TerminalBO source);

    /**
     * Map to Data Transfert Objects.
     *
     * @param source terminal business objects.
     * @return terminals DTO.
     */
    List<TerminalRest> toDto(final List<TerminalBO> source);

    /**
     * Map to Business Object.
     *
     * @param source terminal DTO.
     * @return terminal BO.
     */
    TerminalBO toBusinessObject(final TerminalRest source);

    /**
     * Map to Business Objects.
     *
     * @param source terminal DTOs.
     * @return terminal BOs.
     */
    List<TerminalBO> toBusinessObject(final List<TerminalRest> source);
}
