/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apiindicator.mapper;

import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalServiceNetworkIndicatorView;
import com.soprasteria.g4it.backend.apiindicator.model.DigitalServiceNetworkImpactBO;
import com.soprasteria.g4it.backend.apiindicator.model.DigitalServiceNetworkIndicatorBO;
import com.soprasteria.g4it.backend.apiindicator.utils.CriteriaUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface DigitalServiceNetworkIndicatorMapper {

    /**
     * @param source
     * @return
     */
    List<DigitalServiceNetworkImpactBO> toImpact(final List<DigitalServiceNetworkIndicatorView> source);

    @Mapping(target = "impacts", source = "source")
    default List<DigitalServiceNetworkIndicatorBO> toDto(List<DigitalServiceNetworkIndicatorView> source) {
        final Map<String, List<DigitalServiceNetworkIndicatorView>> collection = source.stream().collect(Collectors.groupingBy(DigitalServiceNetworkIndicatorView::getCriteria));
        return collection.entrySet().stream().map(
                entry ->
                        DigitalServiceNetworkIndicatorBO.builder()
                                .criteria(CriteriaUtils.transformCriteriaNameToCriteriaKey(entry.getKey()))
                                .impacts(toImpact(entry.getValue()))
                                .build()
        ).collect(Collectors.toList());
    }

}
