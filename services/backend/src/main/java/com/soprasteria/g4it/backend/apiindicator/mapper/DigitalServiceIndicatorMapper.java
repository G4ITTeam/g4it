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
import com.soprasteria.g4it.backend.common.utils.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 */
@Mapper(componentModel = "spring")
public interface DigitalServiceIndicatorMapper {

    List<DigitalServiceImpactBO> toImpact(final List<DigitalServiceIndicatorView> source);

    default DigitalServiceImpactBO map(final DigitalServiceIndicatorView digitalServiceIndicatorView) {
        String criterion = CriteriaUtils.transformCriteriaNameToCriteriaKey(digitalServiceIndicatorView.getCriteria());
        if (criterion.isEmpty()) criterion = StringUtils.snakeToKebabCase(digitalServiceIndicatorView.getCriteria());

        return DigitalServiceImpactBO.builder()
                .unitValue(digitalServiceIndicatorView.getUnitValue())
                .unit(digitalServiceIndicatorView.getUnit())
                .sipValue(digitalServiceIndicatorView.getSipValue())
                .criteria(criterion)
                .status(digitalServiceIndicatorView.getStatus())
                .countValue(digitalServiceIndicatorView.getCountValue())
                .build();
    }

    /**
     * @param source
     * @return
     */
    @Mapping(target = "impacts", source = "source")
    default List<DigitalServiceIndicatorBO> toDto(final List<DigitalServiceIndicatorView> source) {
        final Map<String, List<DigitalServiceIndicatorView>> indicatorsByTiers = source.stream()
                .collect(Collectors.groupingBy(DigitalServiceIndicatorView::getTier));
        return indicatorsByTiers
                .entrySet()
                .stream()
                .<DigitalServiceIndicatorBO>map(entry -> DigitalServiceIndicatorBO.builder()
                        .tier(entry.getKey())
                        .impacts(toImpact(entry.getValue()))
                        .build()
                ).toList();
    }
}
