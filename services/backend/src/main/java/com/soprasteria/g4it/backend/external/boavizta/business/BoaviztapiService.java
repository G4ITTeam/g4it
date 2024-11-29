/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.external.boavizta.business;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.soprasteria.g4it.backend.apiinout.modeldb.InVirtualEquipment;
import com.soprasteria.g4it.backend.exception.ExternalApiException;
import com.soprasteria.g4it.backend.external.boavizta.client.BoaviztapiClient;
import com.soprasteria.g4it.backend.external.boavizta.model.request.BoaRequestRest;
import com.soprasteria.g4it.backend.external.boavizta.model.request.BoaTimeWorkloadRest;
import com.soprasteria.g4it.backend.external.boavizta.model.request.BoaUsageRest;
import com.soprasteria.g4it.backend.external.boavizta.model.response.BoaResponseRest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
@Slf4j
public class BoaviztapiService {

    /**
     * BoaviztapiClient
     */
    private BoaviztapiClient boaviztapiClient;

    private static final int USE_TIME_RATIO = 1;

    /**
     * hours life time is calculated by h5 * 365 * 24;
     */
    private static final int HOURS_LIFE_TIME = 43800;

    public static final String BOAVIZTAPI_VERSION = "1.3";

    public static final String BOAVIZTAPI_ENGINE = "BoaviztAPI";

    /**
     * Get BoaviztAPI countries with code.
     *
     * @return country map.
     */
    @Cacheable("boaviztaGetCountryMap")
    public Map<String, String> getCountryMap() {
        return boaviztapiClient.getAllCountries();
    }

    /**
     * Get BoaviztAPI cloud providers.
     *
     * @return providers name list (string).
     */
    @Cacheable("boaviztaGetProviderList")
    public List<String> getProviderList() {
        return boaviztapiClient.getAllProviders();
    }

    /**
     * Get BoaviztAPI instances for one specific cloud provider.
     *
     * @return instances name list (string).
     */
    @Cacheable("boaviztaGetInstanceList")
    public List<String> getInstanceList(String cloudProvider) {
        return boaviztapiClient.getAllInstances(cloudProvider);
    }

    /**
     * Run boavitAPI calculations
     *
     * @param virtualEquipment the virtual equipment
     * @return the Boa response
     */
    public BoaResponseRest runBoaviztCalculations(InVirtualEquipment virtualEquipment) {

        BoaRequestRest request = BoaRequestRest.builder()
                .provider(virtualEquipment.getProvider())
                .instanceType(virtualEquipment.getInstanceType())
                .usage(BoaUsageRest.builder()
                        .usageLocation(virtualEquipment.getLocation())
                        .useTimeRatio(USE_TIME_RATIO)
                        .hoursLifeTime(HOURS_LIFE_TIME)
                        .timeWorkload(List.of(
                                BoaTimeWorkloadRest.builder()
                                        .timePercentage(100)
                                        .loadPercentage((int) (virtualEquipment.getWorkload() * 100))
                                        .build()
                        ))
                        .build())
                .build();
        try {
            return boaviztapiClient.runCalculation(virtualEquipment.getDurationHour(), request);
        } catch (ExternalApiException e) {
            try {
                log.error("Error calling boaviztAPI with body {}", new ObjectMapper().writeValueAsString(request));
            } catch (JsonProcessingException ignored) {
                log.error("Error calling boaviztAPI with body {}", request);
            }
        }
        return null;
    }

}