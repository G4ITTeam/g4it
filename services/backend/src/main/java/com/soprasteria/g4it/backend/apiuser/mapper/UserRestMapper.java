/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apiuser.mapper;

import com.soprasteria.g4it.backend.apiuser.model.UserBO;
import com.soprasteria.g4it.backend.server.gen.api.dto.UserRest;
import org.mapstruct.Mapper;

/**
 * UserRest Mapper.
 */
@Mapper(componentModel = "spring", uses = SubscriberRestMapper.class)
public interface UserRestMapper {

    /**
     * Map a business object to dto object.
     *
     * @param businessObject the source.
     * @return the UserRest.
     */
    UserRest toDto(final UserBO businessObject);
}
