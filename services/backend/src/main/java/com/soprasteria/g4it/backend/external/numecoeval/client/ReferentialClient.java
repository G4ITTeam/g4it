/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.external.numecoeval.client;

import com.soprasteria.g4it.backend.client.gen.connector.apireferentiel.ImportRfrentielsApi;
import com.soprasteria.g4it.backend.client.gen.connector.apireferentiel.InterneNumEcoEvalApi;
import com.soprasteria.g4it.backend.client.gen.connector.apireferentiel.dto.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
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
     * NumEcoEval Referential Imports API.
     */
    private ImportRfrentielsApi importRefrentialApi;

    /**
     * Get all criteria
     * Used to check if any referential has been uploaded
     *
     * @return the list of criteria
     */
    public List<CritereDTO> getAllCriteria() {
        return Objects.requireNonNull(interneNumEcoEvalApi.getAllCriteresWithHttpInfo().block()).getBody();
    }


    public List<EtapeDTO> getAllLifecycleSteps() {
        return Objects.requireNonNull(interneNumEcoEvalApi.getAllEtapesWithHttpInfo().block()).getBody();
    }

    public List<TypeItemDTO> getAllItemTypes() {
        return Objects.requireNonNull(interneNumEcoEvalApi.getAllTypeItemWithHttpInfo().block()).getBody();
    }

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
     * Import criteria in NumEcoEval
     *
     * @param file the csv file
     * @return the report
     */
    public RapportImportDTO importCriteria(File file) {
        return importRefrentialApi.importCriteresCSV(file).block();
    }

    /**
     * Import lifecycle steps in NumEcoEval
     *
     * @param file the csv file
     * @return the report
     */
    public RapportImportDTO importLifecycleSteps(File file) {
        return importRefrentialApi.importEtapesCSV(file).block();
    }

    /**
     * Import hypotheses in NumEcoEval
     *
     * @param file the csv file
     * @return the report
     */
    public RapportImportDTO importHypotheses(File file) {
        return importRefrentialApi.importHypothesesCSV(file).block();
    }

    /**
     * Import characterization factors in NumEcoEval
     *
     * @param file the csv file
     * @return the report
     */
    public RapportImportDTO importCharacterizationFactors(File file) {
        return importRefrentialApi.importFacteurCaracterisationCSV(file, "FULL").block();
    }


    /**
     * Import Type items in NumEcoEval
     *
     * @param file the csv file
     * @return the report
     */
    public RapportImportDTO importItemTypes(File file) {
        return importRefrentialApi.importTypeItemCSV(file).block();
    }

    /**
     * Import Equipment Ref Matching in NumEcoEval
     *
     * @param file the csv file
     * @return the report
     */
    public RapportImportDTO importEquipmentRefMatching(File file) {
        return importRefrentialApi.importCorrespondanceRefEquipementCSV(file).block();
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
