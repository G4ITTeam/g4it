/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apireferential.mapper;

import com.soprasteria.g4it.backend.apireferential.model.AnalysisTableBO;
import com.soprasteria.g4it.backend.server.gen.api.dto.AnalysisTableRest;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AnalysisTableRestMapper {
    
    List<AnalysisTableRest> toRest(final List<AnalysisTableBO> analysisTableBO);

}
