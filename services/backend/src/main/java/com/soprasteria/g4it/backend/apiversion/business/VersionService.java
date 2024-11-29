/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiversion.business;


import com.soprasteria.g4it.backend.external.numecoeval.client.ReferentialClient;
import com.soprasteria.g4it.backend.server.gen.api.dto.VersionRest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Version Service
 */
@Service
@Slf4j
public class VersionService {

    @Value("${version}")
    private String version;

    @Value("${boaviztapi.version}")
    private String boaviztaVersion;

    @Autowired
    private ReferentialClient referentialClient;

    /**
     * Get the NumEcoEval version
     *
     * @return the numEcoEval version
     */
    @Cacheable("getVersion")
    public VersionRest getVersion() {
        String numEcoEvalVersion = null;

        try {
            numEcoEvalVersion = referentialClient.getVersion();
        } catch (Exception e) {
            log.error("Cannot connect to api-referential, or retrieve version. Error: {}", e.getMessage());
        }

        return VersionRest.builder()
                .numEcoEval(numEcoEvalVersion)
                .boaviztapi(boaviztaVersion)
                .g4it(version)
                .build();
    }
}
