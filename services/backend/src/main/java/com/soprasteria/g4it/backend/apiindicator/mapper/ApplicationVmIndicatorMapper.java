/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apiindicator.mapper;

import com.soprasteria.g4it.backend.apiindicator.model.ApplicationIndicatorBO;
import com.soprasteria.g4it.backend.apiindicator.model.ApplicationVmImpactBO;
import com.soprasteria.g4it.backend.apiindicator.modeldb.ApplicationVmIndicatorView;
import com.soprasteria.g4it.backend.apiindicator.utils.CriteriaUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.ArrayList;
import java.util.List;

/**
 * Application indicator mapper.
 */
@Mapper(componentModel = "spring")
public interface ApplicationVmIndicatorMapper {

    /**
     * Map application VM indicator view to impact business object.
     *
     * @param source data from database.
     * @return the impacts.
     */
    List<ApplicationVmImpactBO> toImpact(final List<ApplicationVmIndicatorView> source);

    /**
     * Map application VM indicator view to impact business object.
     *
     * @param source data from database.
     * @return the impacts.
     */
    ApplicationVmImpactBO toImpact(final ApplicationVmIndicatorView source);

    /**
     * Map application VM indicators to indicator business object.
     *
     * @param source data from database.
     * @return the indicator business object.
     */
    @Mapping(target = "impacts", source = "source")
    default List<ApplicationIndicatorBO<ApplicationVmImpactBO>> toDto(final List<ApplicationVmIndicatorView> source) {
        if (CollectionUtils.isEmpty(source)) {
            return new ArrayList<>();
        }
        // Get the first item to retrieve criteria and unit.
        final ApplicationVmIndicatorView firstItem = source.get(0);
        return List.of(ApplicationIndicatorBO.<ApplicationVmImpactBO>builder()
                .criteria(CriteriaUtils.transformCriteriaNameToCriteriaKey(firstItem.getCriteria()))
                .unit(firstItem.getUnit())
                .impacts(toImpact(source))
                .build());
    }

}
