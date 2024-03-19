/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.external.numecoeval.client;

import com.soprasteria.g4it.backend.client.gen.connector.apireferentiel.InterneNumEcoEvalApi;
import com.soprasteria.g4it.backend.client.gen.connector.apireferentiel.dto.MixElectriqueDTO;
import com.soprasteria.g4it.backend.client.gen.connector.apireferentiel.dto.TypeEquipementDTO;
import com.soprasteria.g4it.backend.client.gen.connector.apireferentiel.dto.VersionDTO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * NumEcoEval external client connecting to api-referentiel
 */
@Service
@AllArgsConstructor
public class ReferentialClient {

    /**
     * NumEcoEval Referential API.
     */
    private InterneNumEcoEvalApi interneNumEcoEvalApi;

    /**
     * Get countries from referential - mix electriques
     *
     * @return the list of mix elec
     */
    public List<MixElectriqueDTO> getMixElec() {
        return Objects.requireNonNull(interneNumEcoEvalApi.getMixElectriqueParPaysWithHttpInfo("_all").block()).getBody();
    }

    /**
     * Get equipment types from referential - type equipement
     *
     * @return the list of equipment types
     */
    public List<TypeEquipementDTO> getEquipementTypes() {
        return Objects.requireNonNull(interneNumEcoEvalApi.getAllTypeEquipementWithHttpInfo().block()).getBody();
    }

    /**
     * Get NumEcoEval version
     *
     * @return the version
     */
    public String getVersion() {
        final VersionDTO response = interneNumEcoEvalApi.getVersion().block();
        return response == null ? null : response.getVersion();
    }
}
