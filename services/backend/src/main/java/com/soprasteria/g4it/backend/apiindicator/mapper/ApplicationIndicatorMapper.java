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
import com.soprasteria.g4it.backend.apiindicator.modeldb.AggApplicationIndicator;
import com.soprasteria.g4it.backend.apiindicator.utils.CriteriaUtils;
import com.soprasteria.g4it.backend.apiinout.modeldb.OutApplication;
import com.soprasteria.g4it.backend.common.utils.StringUtils;
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
     * Map aggregated application indicator to impact business object.
     *
     * @param source data from database.
     * @return the impacts.
     */
    List<ApplicationImpactBO> toImpact(final List<AggApplicationIndicator> source);

    List<ApplicationImpactBO> toOutImpact(final List<OutApplication> source);

    @Mapping(target = "lifeCycle", source = "lifecycleStep")
    @Mapping(target = "environment", source = "environment")
    @Mapping(target = "applicationName", source = "name")
    @Mapping(target = "impact", source = "unitImpact")
    @Mapping(target = "sip", source = "peopleEqImpact")
    @Mapping(target = "statusIndicator", source = "statusIndicator")
    @Mapping(target = "domain", expression = "java(source.getFilters().get(0))")
    @Mapping(target = "subDomain", expression = "java(source.getFilters().get(1))")
    @Mapping(target = "cluster", expression = "java(source.getFiltersVirtualEquipment().get(0))")
    ApplicationImpactBO toOutImpact(final OutApplication source);

    /**
     * Map aggregated application indicator to impact business object.
     *
     * @param source data from database.
     * @return the impacts.
     */
    @Mapping(target = "cluster", source = "cluster", defaultValue = "")
    ApplicationImpactBO toImpact(final AggApplicationIndicator source);

    /**
     * Map application indicators to indicator business object.
     *
     * @param source data from database.
     * @return the indicator business object.
     */
    @Mapping(target = "impacts", source = "source")
    default List<ApplicationIndicatorBO<ApplicationImpactBO>> toDto(final List<AggApplicationIndicator> source) {
        return source.stream().collect(Collectors.groupingBy(ind -> Pair.of(ind.getCriteria(), ind.getUnit())))
                .entrySet().stream().
                        <ApplicationIndicatorBO<ApplicationImpactBO>>map(entry -> ApplicationIndicatorBO.builder()
                        .criteria(CriteriaUtils.transformCriteriaNameToCriteriaKey(entry.getKey().getKey()))
                        .unit(entry.getKey().getValue())
                        .impacts(toImpact(entry.getValue()))
                        .build())
                .toList();
    }

    @Mapping(target = "impacts", source = "source")
    default List<ApplicationIndicatorBO<ApplicationImpactBO>> toOutDto(final List<OutApplication> source) {
        return source.stream().collect(Collectors.groupingBy(ind -> Pair.of(ind.getCriterion(), ind.getUnit())))
                .entrySet().stream().
                        <ApplicationIndicatorBO<ApplicationImpactBO>>map(entry -> ApplicationIndicatorBO.builder()
                        .criteria(StringUtils.snakeToKebabCase(entry.getKey().getKey()))
                        .unit(entry.getKey().getValue())
                        .impacts(toOutImpact(entry.getValue()))
                        .build())
                .toList();
    }

}
