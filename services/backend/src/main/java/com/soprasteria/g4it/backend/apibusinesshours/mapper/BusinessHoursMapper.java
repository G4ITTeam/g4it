/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apibusinesshours.mapper;

import com.soprasteria.g4it.backend.apibusinesshours.modeldb.BusinessHours;
import com.soprasteria.g4it.backend.server.gen.api.dto.BusinessHoursRest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BusinessHoursMapper {

    @Mapping(source = "startTime", target = "startTime", dateFormat = "h:mm a")
    @Mapping(source = "endTime", target = "endTime", dateFormat = "h:mm a")
    @Mapping(source = "weekday", target = "day")
    BusinessHoursRest getModelFromEntity(BusinessHours entity);

    @Mapping(source = "manufacturingDate", target = "manufacturingDate", dateFormat = "dd.MM.yyyy")
    List<BusinessHoursRest> toBusinessHoursRest(final List<BusinessHours> source);
}
