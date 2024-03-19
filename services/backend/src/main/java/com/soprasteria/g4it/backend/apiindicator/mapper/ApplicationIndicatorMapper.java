/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apiindicator.mapper;

import com.soprasteria.g4it.backend.apiindicator.model.ApplicationImpactBO;
import com.soprasteria.g4it.backend.apiindicator.model.ApplicationIndicatorBO;
import com.soprasteria.g4it.backend.apiindicator.modeldb.ApplicationIndicatorView;
import com.soprasteria.g4it.backend.apiindicator.utils.CriteriaUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Application indicator mapper.
 */
@Mapper(componentModel = "spring")
public interface ApplicationIndicatorMapper {

    /**
     * Map application indicator view to impact business object.
     *
     * @param source data from database.
     * @return the impacts.
     */
    List<ApplicationImpactBO> toImpact(final List<ApplicationIndicatorView> source);

    /**
     * Map application indicator view to impact business object.
     *
     * @param source data from database.
     * @return the impacts.
     */
    ApplicationImpactBO toImpact(final ApplicationIndicatorView source);

    /**
     * Map application indicators to indicator business object.
     *
     * @param source data from database.
     * @return the indicator business object.
     */
    @Mapping(target = "impacts", source = "source")
    default List<ApplicationIndicatorBO<ApplicationImpactBO>> toDto(final List<ApplicationIndicatorView> source) {
        return source.stream().collect(Collectors.groupingBy(ind -> Pair.of(ind.getCriteria(), ind.getUnit())))
                .entrySet().stream().map(entry -> ApplicationIndicatorBO.builder()
                        .criteria(CriteriaUtils.transformCriteriaNameToCriteriaKey(entry.getKey().getKey()))
                        .unit(entry.getKey().getValue())
                        .impacts(toImpact(entry.getValue()))
                        .build()).collect(Collectors.toList());
    }

}
