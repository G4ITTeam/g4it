/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiinout.mapper;

import com.soprasteria.g4it.backend.apiinout.modeldb.OutApplication;
import com.soprasteria.g4it.backend.server.gen.api.dto.OutApplicationRest;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * Out application mapper.
 */
@Mapper(componentModel = "spring")
public interface OutApplicationMapper {

    List<OutApplicationRest> toRest(final List<OutApplication> source);
}
