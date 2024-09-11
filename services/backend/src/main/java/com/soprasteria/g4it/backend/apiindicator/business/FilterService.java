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
import com.soprasteria.g4it.backend.apiindicator.modeldb.Filters;
import com.soprasteria.g4it.backend.apiindicator.repository.ApplicationFiltersRepository;
import com.soprasteria.g4it.backend.apiindicator.repository.EquipmentFiltersRepository;
import com.soprasteria.g4it.backend.apiindicator.utils.Constants;
import com.soprasteria.g4it.backend.apiindicator.utils.TypeUtils;
import com.soprasteria.g4it.backend.apiuser.business.OrganizationService;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

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
     * Extract list of values from filters coming from database
     *
     * @param filters the list of filters
     * @param field   the field to filter on
     * @return the list of values
     */
    private List<String> extractListFromFilters(final List<Filters> filters, final String field) {
        if (filters == null) return List.of();

        return Arrays.stream(filters.stream()
                        .filter(filter -> filter.getField().equals(field))
                        .map(Filters::getValues)
                        .findFirst()
                        .orElse(new String[0]))
                .map(value -> value == null ? "" : value)
                .toList();
    }

    /**
     * Retrieve equipment filters.
     *
     * @param subscriber   the subscriber
     * @param organization the organization.
     * @param batchName    the batch name.
     * @return filters.
     */
    public EquipmentFiltersBO getEquipmentFilters(final String subscriber, final Organization organization, final String batchName) {
        final List<Filters> equipmentFiltersList = equipmentFiltersRepository.getFiltersByBatchName(batchName);
        return EquipmentFiltersBO.builder()
                .status(extractListFromFilters(equipmentFiltersList, "status"))
                .countries(extractListFromFilters(equipmentFiltersList, "country"))
                .equipments(extractListFromFilters(equipmentFiltersList, "type").stream()
                        .map(value -> TypeUtils.getShortType(subscriber, organization.getName(), value))
                        .toList())
                .entities(extractListFromFilters(equipmentFiltersList, "entity"))
                .build();
    }


    /**
     * Retrieve application filters.
     *
     * @param subscriber      the subscriber
     * @param organizationId  the organization id
     * @param batchName       the num-eco-eval batch name.
     * @param domain          the domain
     * @param subDomain       the sub domain
     * @param applicationName the application name
     * @return applications filters.
     */
    public ApplicationFiltersBO getApplicationFilters(final String subscriber,
                                                      final Long organizationId,
                                                      final String batchName,
                                                      final String domain, final String subDomain, final String applicationName) {
        List<Filters> applicationFilters;
        if (applicationName == null) {
            applicationFilters = applicationFiltersRepository.getFiltersByBatchName(batchName);
        } else {
            applicationFilters = applicationFiltersRepository.getFiltersByBatchNameAndApplicationName(batchName, applicationName);
        }

        final Organization organization = organizationService.getOrganizationById(organizationId);

        ApplicationFiltersBO result = ApplicationFiltersBO.builder()
                .environments(extractListFromFilters(applicationFilters, "environment"))
                .lifeCycles(extractListFromFilters(applicationFilters, "life_cycle"))
                .types(extractListFromFilters(applicationFilters, "type").stream()
                        .map(value -> TypeUtils.getShortType(subscriber, organization.getName(), value))
                        .toList())
                .domains(buildDomains(extractListFromFilters(applicationFilters, "domain")))
                .build();

        if (domain != null) {
            final String localDomain = domain.equals(Constants.UNSPECIFIED) ? "" : domain;
            result.setDomains(result.getDomains().stream().filter(val -> localDomain.equals(val.getName())).toList());
        }

        if (subDomain != null) {
            final String localSubDomain = subDomain.equals(Constants.UNSPECIFIED) ? "" : subDomain;
            result.setDomains(result.getDomains().stream()
                    .peek(val -> val.setSubDomains(val.getSubDomains().stream().filter(localSubDomain::equals).toList()))
                    .toList());
        }

        return result;
    }

    /**
     * Build the domain list.
     *
     * @param domainsAndSubDomains the list of domain and subdomains : "domain||sub domain1##sub domain2"
     * @return the domains filters list.
     */
    private List<ApplicationDomainsFiltersBO> buildDomains(List<String> domainsAndSubDomains) {
        return domainsAndSubDomains.stream().map(domainAndSubDomains -> {
                    String[] domainSplit = domainAndSubDomains.split("\\|\\|");
                    ApplicationDomainsFiltersBO applicationDomainsFiltersBO = ApplicationDomainsFiltersBO.builder()
                            .name(domainSplit[0])
                            .subDomains(Arrays.stream(domainSplit[1].split("##")).toList())
                            .build();
                    return applicationDomainsFiltersBO;
                })
                .toList();

    }
}
