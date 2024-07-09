/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiindicator.business;

import com.soprasteria.g4it.backend.apiindicator.model.ApplicationDomainsFiltersBO;
import com.soprasteria.g4it.backend.apiindicator.model.ApplicationFiltersBO;
import com.soprasteria.g4it.backend.apiindicator.model.EquipmentFiltersBO;
import com.soprasteria.g4it.backend.apiindicator.modeldb.ApplicationFilters;
import com.soprasteria.g4it.backend.apiindicator.modeldb.EquipmentFilters;
import com.soprasteria.g4it.backend.apiindicator.repository.ApplicationFiltersRepository;
import com.soprasteria.g4it.backend.apiindicator.repository.EquipmentFiltersRepository;
import com.soprasteria.g4it.backend.apiindicator.utils.Constants;
import com.soprasteria.g4it.backend.apiindicator.utils.TypeUtils;
import com.soprasteria.g4it.backend.apiuser.business.OrganizationService;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * Filters service.
 */
@Service
@AllArgsConstructor
public class FilterService {

    /**
     * Repository to access equipment indicator filters data.
     */
    private EquipmentFiltersRepository equipmentFiltersRepository;

    /**
     * Repository to access application indicator filters data.
     */
    private ApplicationFiltersRepository applicationFiltersRepository;

    /**
     * The Organization Service
     */
    private OrganizationService organizationService;

    /**
     * Retrieve equipment filters.
     *
     * @param subscriber   the subscriber
     * @param organization the organization.
     * @param inventoryId  the inventory id.
     * @param batchName    the batch name.
     * @return filters.
     */
    public EquipmentFiltersBO getEquipmentFilters(final String subscriber, final Organization organization, final Long inventoryId, final String batchName) {
        final List<EquipmentFilters> equipmentFiltersList = equipmentFiltersRepository.getFiltersByInventoryId(inventoryId, batchName);
        return EquipmentFiltersBO.builder()
                .status(equipmentFiltersList.stream().map(EquipmentFilters::getStatus).distinct().toList())
                .countries(equipmentFiltersList.stream().map(EquipmentFilters::getCountry).distinct().toList())
                .equipments(equipmentFiltersList.stream().map(equipmentFilters -> TypeUtils.getShortType(subscriber, organization.getName(), equipmentFilters.getType())).distinct().toList())
                .entities(equipmentFiltersList.stream().map(EquipmentFilters::getEntity).distinct().toList())
                .build();
    }


    /**
     * Retrieve application filters.
     *
     * @param subscriber      the subscriber
     * @param organizationId  the organization id
     * @param inventoryId     the inventory unique identifier.
     * @param batchName       the num-eco-eval batch name.
     * @param domain          the domain
     * @param subDomain       the sub domain
     * @param applicationName the application name
     * @return applications filters.
     */
    public ApplicationFiltersBO getApplicationFilters(final String subscriber,
                                                      final Long organizationId,
                                                      final Long inventoryId, final String batchName,
                                                      final String domain, final String subDomain, final String applicationName) {
        List<ApplicationFilters> applicationFilters;
        if (applicationName == null) {
            applicationFilters = applicationFiltersRepository.getFiltersByBatchName(inventoryId, batchName);
        } else {
            applicationFilters = applicationFiltersRepository.getFiltersByBatchNameAndApplicationName(inventoryId, batchName, applicationName);
        }

        final Organization linkedOrganization = organizationService.getOrganizationById(organizationId);

        List<ApplicationFilters> filteredApplicationFilters = applicationFilters.stream()
                .peek(applicationFilters1 -> applicationFilters1.setType(TypeUtils.getShortType(subscriber, linkedOrganization.getName(), applicationFilters1.getType())))
                .toList();

        if (domain != null) {
            final String localDomain = domain.equals(Constants.UNSPECIFIED) ? "" : domain;
            filteredApplicationFilters = filteredApplicationFilters.stream().filter(filter -> localDomain.equals(filter.getDomain())).toList();
        }
        if (subDomain != null) {
            final String localSubDomain = subDomain.equals(Constants.UNSPECIFIED) ? "" : subDomain;
            filteredApplicationFilters = filteredApplicationFilters.stream().filter(filter -> localSubDomain.equals(filter.getSubDomain())).toList();
        }

        return ApplicationFiltersBO.builder()
                .environments(filteredApplicationFilters.stream().map(ApplicationFilters::getEnvironment).distinct().toList())
                .lifeCycles(filteredApplicationFilters.stream().map(ApplicationFilters::getLifeCycle).distinct().toList())
                .domains(buildDomains(filteredApplicationFilters.stream().collect(Collectors.groupingBy(ApplicationFilters::getDomain))))
                .types(filteredApplicationFilters.stream().map(ApplicationFilters::getType).distinct().toList())
                .build();
    }

    /**
     * Build the domain list.
     *
     * @param filtersByDomain the map containing the domain in key, and the attached filters list.
     * @return the domains filters list.
     */
    private List<ApplicationDomainsFiltersBO> buildDomains(final Map<String, List<ApplicationFilters>> filtersByDomain) {
        return filtersByDomain.entrySet().stream().map(entry -> ApplicationDomainsFiltersBO.builder()
                .name(entry.getKey())
                .subDomains(entry.getValue().stream().map(ApplicationFilters::getSubDomain).distinct().toList())
                .build()).collect(toList());
    }

}
