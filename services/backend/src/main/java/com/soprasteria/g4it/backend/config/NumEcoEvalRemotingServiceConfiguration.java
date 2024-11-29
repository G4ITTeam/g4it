/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.config;

import com.soprasteria.g4it.backend.client.gen.connector.apiexposition.CalculsApi;
import com.soprasteria.g4it.backend.client.gen.connector.apiexposition.ImportsApi;
import com.soprasteria.g4it.backend.client.gen.connector.apiexposition.invoker.ApiClient;
import com.soprasteria.g4it.backend.client.gen.connector.apireferentiel.ImportRfrentielsApi;
import com.soprasteria.g4it.backend.client.gen.connector.apireferentiel.InterneNumEcoEvalApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * NumEcoEval Remoting configuration.
 */
@Configuration
public class NumEcoEvalRemotingServiceConfiguration {

    @Value("${num-eco-eval.base-url}")
    private String numEcoEvalBaseUrl;
    @Value("${num-eco-eval.import-max-memory}")
    private Integer importMaxMemory;
    @Value("${num-eco-eval-referential.base-url}")
    private String numEcoEvalReferentialBaseUrl;

    /**
     * Creates a Bean for NumEcoEval API calcul configuration
     *
     * @return CalculsApi invoker
     */
    @Bean
    public CalculsApi clientCalculsApi() {
        final var apiClient = new ApiClient(WebClient.builder().baseUrl(numEcoEvalBaseUrl).build());
        apiClient.setBasePath(numEcoEvalBaseUrl);

        return new CalculsApi(apiClient);
    }

    /**
     * Creates a Bean for NumEcoEval API import csv configuration
     *
     * @return ImportsApi invoker
     */
    @Bean
    public ImportsApi clientImportsApi() {
        final var apiClient = new ApiClient(WebClient.builder()
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(importMaxMemory * 1024 * 1024))
                .baseUrl(numEcoEvalBaseUrl).build());
        apiClient.setBasePath(numEcoEvalBaseUrl);

        return new ImportsApi(apiClient);
    }

    /**
     * Creates a Bean for NumEcoEval API referential
     *
     * @return InterneNumEcoEvalApi invoker
     */
    @Bean
    public InterneNumEcoEvalApi clientReferentialApi() {
        final var apiClient = new com.soprasteria.g4it.backend.client.gen.connector.apireferentiel.invoker.ApiClient(WebClient.builder().baseUrl(numEcoEvalReferentialBaseUrl).build());
        apiClient.setBasePath(numEcoEvalReferentialBaseUrl);

        return new InterneNumEcoEvalApi(apiClient);
    }

    /**
     * Creates a Bean for NumEcoEval import API referential
     *
     * @return InterneNumEcoEvalApi invoker
     */
    @Bean
    public ImportRfrentielsApi clientReferentialImportApi() {
        final var apiClient = new com.soprasteria.g4it.backend.client.gen.connector.apireferentiel.invoker.ApiClient(WebClient.builder().baseUrl(numEcoEvalReferentialBaseUrl).build());
        apiClient.setBasePath(numEcoEvalReferentialBaseUrl);

        return new ImportRfrentielsApi(apiClient);
    }

}
