/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.external.boavizta.client;

import com.soprasteria.g4it.backend.exception.ExternalApiException;
import com.soprasteria.g4it.backend.external.boavizta.model.request.BoaRequestRest;
import com.soprasteria.g4it.backend.external.boavizta.model.response.BoaResponseRest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class BoaviztapiClient {

    @Autowired
    @Qualifier("webClientBoaviztapi")
    WebClient webClient;

    private static final String CRITERIA = "criteria";

    /**
     * Get all available countries from BoaviztAPI
     *
     * @return a map of (country, country_trigram)
     */
    public Map<String, String> getAllCountries() {
        try {
            Map<String, String> response = webClient.get().uri("/v1/utils/country_code").retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, String>>() {
                    }).block();
            if (response == null) {
                throw new ExternalApiException("boavitzapi-no-country-found");
            }
            return response;
        } catch (WebClientRequestException e) {
            throw new ExternalApiException("boavitzapi-no-country-found");
        }
    }

    /**
     * Get all available cloud providers from BoaviztAPI
     *
     * @return the list of cloud providers
     */
    public List<String> getAllProviders() {
        try {
            String[] response = webClient.get().uri("/v1/cloud/instance/all_providers").retrieve().bodyToMono(String[].class).block();
            if (response == null) {
                throw new ExternalApiException("boavitzapi-no-cloud-provider-found");
            }
            return Arrays.asList(response);
        } catch (WebClientRequestException e) {
            throw new ExternalApiException("boavitzapi-no-cloud-provider-found");
        }
    }

    /**
     * Get all available instances for one specific cloud provider from BoaviztAPI
     *
     * @return the list of instances name
     */
    public List<String> getAllInstances(String cloudProvider) {
        try {
            String[] response = webClient.get().uri("/v1/cloud/instance/all_instances?provider=" + cloudProvider).retrieve().bodyToMono(String[].class).block();
            if (response == null) {
                throw new ExternalApiException("boavitzapi-no-instance-found");
            }
            return Arrays.asList(response);
        } catch (WebClientRequestException e) {
            throw new ExternalApiException("boavitzapi-no-instance-found");
        }
    }

    /**
     * Run calcaulation on BoavitzAPI
     *
     * @param duration    the usage duration
     * @param requestBody the request body
     * @return the response
     */
    public BoaResponseRest runCalculation(Double duration, BoaRequestRest requestBody) {

        UriComponents uriComponents = UriComponentsBuilder.newInstance()
                .path("/v1/cloud/instance")
                .queryParam("verbose", false)
                .queryParam("duration", duration)
                .queryParam(CRITERIA, "gwp")
                .queryParam(CRITERIA, "adp")
                .queryParam(CRITERIA, "pe")
                .build();
        try {
            BoaResponseRest response = webClient.post()
                    .uri(uriComponents.toUriString())
                    .accept(MediaType.APPLICATION_JSON)
                    .body(Mono.just(requestBody), BoaRequestRest.class)
                    .retrieve()
                    .bodyToMono(BoaResponseRest.class)
                    .block();

            if (response == null) {
                throw new ExternalApiException("boavitzapi-calculation-failed");
            }
            return response;
        } catch (WebClientRequestException | WebClientResponseException e) {
            throw new ExternalApiException("boavitzapi-calculation-failed");
        }
    }
}
