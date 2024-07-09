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
import com.soprasteria.g4it.backend.apiindicator.utils.TypeUtils;
import com.soprasteria.g4it.backend.apiuser.business.OrganizationService;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
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


    @Autowired
    private OrganizationService organizationService;
    /**
     * DataCenter indicators mapper.
     */
    @Autowired
    private DataCenterIndicatorMapper dataCenterIndicatorMapper;

    /**
     * Retrieve datacenter indicators.
     *
     * @param subscriber     the subscriber.
     * @param organizationId the organization's id.
     * @param inventoryId    the inventory id.
     * @return datacenter indicators.
     */
    public List<DataCentersInformationBO> getDataCenterIndicators(final String subscriber, final Long organizationId, final Long inventoryId) {
        final Organization linkedOrganization = organizationService.getOrganizationById(organizationId);
        final var result = dataCenterIndicatorViewRepository.findDataCenterIndicators(inventoryId).stream()
                .peek(indicator -> indicator.setEquipment(TypeUtils.getShortType(subscriber, linkedOrganization.getName(), indicator.getEquipment())))
                .toList();
        return dataCenterIndicatorMapper.toDto(result);
    }
}
