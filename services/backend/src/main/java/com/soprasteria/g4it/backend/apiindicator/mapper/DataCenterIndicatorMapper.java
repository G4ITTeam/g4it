/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apiindicator.mapper;

import com.soprasteria.g4it.backend.apiindicator.model.DataCentersInformationBO;
import com.soprasteria.g4it.backend.apiindicator.modeldb.DataCenterIndicatorView;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DataCenterIndicatorMapper {

    List<DataCentersInformationBO> toDto(final List<DataCenterIndicatorView> source);

}
