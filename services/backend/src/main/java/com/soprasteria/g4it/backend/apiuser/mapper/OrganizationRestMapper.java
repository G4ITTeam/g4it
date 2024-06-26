/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiuser.mapper;

import com.soprasteria.g4it.backend.apidigitalservice.mapper.DateMapper;
import com.soprasteria.g4it.backend.apiuser.model.OrganizationBO;
import com.soprasteria.g4it.backend.server.gen.api.dto.OrganizationRest;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * UserRest Mapper.
 */
@Mapper(componentModel = "spring", uses = {DateMapper.class})
public interface OrganizationRestMapper {

    /**
     * Map a business object to dto object.
     *
     * @param businessObject the source.
     * @return the OrganizationRest.
     */
    OrganizationRest toDto(final OrganizationBO businessObject);

    /**
     * Map a business object list to dto object list.
     *
     * @param businessObject the source.
     * @return the OrganizationRest list.
     */
    List<OrganizationRest> toDto(final List<OrganizationBO> businessObject);


}
