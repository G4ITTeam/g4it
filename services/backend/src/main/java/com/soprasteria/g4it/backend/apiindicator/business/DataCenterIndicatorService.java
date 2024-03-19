/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apiindicator.business;

import com.soprasteria.g4it.backend.apiindicator.mapper.DataCenterIndicatorMapper;
import com.soprasteria.g4it.backend.apiindicator.model.DataCentersInformationBO;
import com.soprasteria.g4it.backend.apiindicator.repository.DataCenterIndicatorViewRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * DataCenter indicator service.
 */
@Service
@AllArgsConstructor
public class DataCenterIndicatorService {

    /**
     * Repository to access data center indicators.
     */
    @Autowired
    private DataCenterIndicatorViewRepository dataCenterIndicatorViewRepository;

    /**
     * DataCenter indicators mapper.
     */
    @Autowired
    private DataCenterIndicatorMapper dataCenterIndicatorMapper;

    /**
     * Retrieve datacenter indicators.
     *
     * @param subscriber   the subscriber.
     * @param organization the organization.
     * @param inventoryId  the inventory id.
     * @return datacenter indicators.
     */
    public List<DataCentersInformationBO> getDataCenterIndicators(final String subscriber, final String organization, final Long inventoryId) {
        return dataCenterIndicatorMapper.toDto(dataCenterIndicatorViewRepository.findDataCenterIndicators(subscriber, organization, inventoryId));
    }
}
