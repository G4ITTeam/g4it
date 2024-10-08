/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.external.numecoeval.client;

import com.soprasteria.g4it.backend.client.gen.connector.apiexposition.CalculsApi;
import com.soprasteria.g4it.backend.client.gen.connector.apiexposition.ImportsApi;
import com.soprasteria.g4it.backend.client.gen.connector.apiexposition.dto.*;
import com.soprasteria.g4it.backend.exception.NumEcoEvalConnectorRuntimeException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDate;
import java.util.List;

/**
 * NumEcoEval external client connecting to api-expositiondonneesentrees
 */
@Service
@AllArgsConstructor
@Slf4j
public class CalculationClient {

    /**
     * NumEcoEval Calcul API.
     */
    private CalculsApi calculsApi;

    /**
     * NumEcoEval Imports Api.
     */
    private ImportsApi importsApi;

    /**
     * Submit calculations of a batch name in with a specific mode, SYNC or ASYNC
     * SYNC mode is used for Digital Services
     *
     * @param batchName the batch name
     * @param modeRest  the mode ASYNC or SYNC
     * @return the RapportDemandeCalculRest
     */
    public RapportDemandeCalculRest submitCalculations(final String batchName, final ModeRest modeRest, final List<String> criteriaList) {
        final var demandeCalculRest = new DemandeCalculRest();
        demandeCalculRest.setNomLot(batchName);
        demandeCalculRest.setEtapes(null);
        demandeCalculRest.setCriteres(criteriaList);
        log.info("Criteria to calculate the evaluation on: {}", criteriaList);

        return calculsApi.soumissionPourCalcul(demandeCalculRest, null, modeRest).block();

    }

    /**
     * Import CSV files into the API of NumEcoEval api-expositiondonneesentrees
     *
     * @param batchName            the batch name
     * @param organizationId       the organization id
     * @param batchDate            the batch date or null
     * @param dataCenterCsv        the csv file datacenter
     * @param physicalEquipmentCsv the csv file physical equipment
     * @param virtualEquipmentCsv  the csv file virtual equipment
     * @param applicationCsv       the csv file application
     * @return the list of RapportImportRest
     */
    public List<RapportImportRest> importCSV(final String batchName, final String organizationId, final LocalDate batchDate,
                                             final File dataCenterCsv,
                                             final File physicalEquipmentCsv,
                                             final File virtualEquipmentCsv,
                                             final File applicationCsv
    ) {
        final String batchDateString = batchDate == null ? null : batchDate.toString();

        final ResponseEntity<List<RapportImportRest>> response = importsApi.importCSVWithHttpInfo(organizationId, batchName, batchDateString, dataCenterCsv,
                        physicalEquipmentCsv, virtualEquipmentCsv, applicationCsv, null, null, null)
                .block();
        if (response == null) {
            throw new NumEcoEvalConnectorRuntimeException("NumEcoEval returned null response when calling POST /importCSV");

        }
        return response.getBody();
    }

    /**
     * Get the status and progress percentage of the calculation of the batch
     *
     * @param batchName      the batch name
     * @param organizationId the organizationId as string
     * @return the StatutCalculRest
     */
    public StatutCalculRest getCalculationsStatus(final String batchName, final String organizationId) {
        return calculsApi.statutPourCalcul(batchName, organizationId).block();
    }

}
