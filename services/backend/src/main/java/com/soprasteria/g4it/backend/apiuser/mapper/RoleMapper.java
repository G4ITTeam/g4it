/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiuser.mapper;

import com.soprasteria.g4it.backend.apiuser.model.RoleBO;
import com.soprasteria.g4it.backend.apiuser.modeldb.Role;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    /**
     * Map a business object list to dto object list.
     *
     * @param source the source.
     * @return the OrganizationRest list.
     */
    List<RoleBO> toDto(final List<Role> source);
    
}
