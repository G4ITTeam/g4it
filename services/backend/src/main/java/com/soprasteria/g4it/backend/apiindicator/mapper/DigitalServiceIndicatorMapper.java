/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiindicator.mapper;

import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalServiceIndicatorView;
import com.soprasteria.g4it.backend.apiindicator.model.DigitalServiceImpactBO;
import com.soprasteria.g4it.backend.apiindicator.model.DigitalServiceIndicatorBO;
import com.soprasteria.g4it.backend.apiindicator.utils.CriteriaUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 *
 */
@Mapper(componentModel = "spring")
public interface DigitalServiceIndicatorMapper {

    /**
     * @param source
     * @return
     */
    List<DigitalServiceImpactBO> toImpact(final List<DigitalServiceIndicatorView> source);

    default DigitalServiceImpactBO map(final DigitalServiceIndicatorView digitalServiceIndicatorView) {
        return DigitalServiceImpactBO.builder()
                .unitValue(digitalServiceIndicatorView.getUnitValue())
                .unit(digitalServiceIndicatorView.getUnit())
                .sipValue(digitalServiceIndicatorView.getSipValue())
                .criteria(CriteriaUtils.transformCriteriaNameToCriteriaKey(digitalServiceIndicatorView.getCriteria()))
                .build();
    }

    /**
     * @param source
     * @return
     */
    @Mapping(target = "impacts", source = "source")
    default List<DigitalServiceIndicatorBO> toDto(final List<DigitalServiceIndicatorView> source) {
        final Map<String, List<DigitalServiceIndicatorView>> indicatorsByTiers = source.stream().collect(Collectors.groupingBy(DigitalServiceIndicatorView::getTier));
        return indicatorsByTiers
                .entrySet()
                .stream()
                .map(entry -> DigitalServiceIndicatorBO.builder()
                        .tier(entry.getKey())
                        .impacts(toImpact(entry.getValue()))
                        .build()
                ).collect(toList());
    }
}
