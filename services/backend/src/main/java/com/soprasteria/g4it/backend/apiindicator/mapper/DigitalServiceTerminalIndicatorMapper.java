/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiindicator.mapper;

import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalServiceTerminalIndicatorView;
import com.soprasteria.g4it.backend.apiindicator.model.DigitalServiceTerminalImpactBO;
import com.soprasteria.g4it.backend.apiindicator.model.DigitalServiceTerminalIndicatorBO;
import com.soprasteria.g4it.backend.apiindicator.utils.CriteriaUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface DigitalServiceTerminalIndicatorMapper {

    /**
     * @param source
     * @return
     */
    List<DigitalServiceTerminalImpactBO> toImpact(final List<DigitalServiceTerminalIndicatorView> source);

    /**
     * @param source
     * @return List of digital service Terminal indicators view BO
     */
    @Mapping(target = "impacts", source = "source")
    default List<DigitalServiceTerminalIndicatorBO> toDto(final List<DigitalServiceTerminalIndicatorView> source) {
        return source.stream()
                .collect(Collectors.groupingBy(DigitalServiceTerminalIndicatorView::getCriteria))
                .entrySet().stream()
                .<DigitalServiceTerminalIndicatorBO>map(entry -> DigitalServiceTerminalIndicatorBO.builder()
                        .criteria(CriteriaUtils.transformCriteriaNameToCriteriaKey(entry.getKey()))
                        .impacts(toImpact(entry.getValue()))
                        .build())
                .toList();
    }
}
