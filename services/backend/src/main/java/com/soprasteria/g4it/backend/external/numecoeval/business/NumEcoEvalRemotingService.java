/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.external.numecoeval.business;


import com.soprasteria.g4it.backend.apiindicator.utils.Constants;
import com.soprasteria.g4it.backend.apiindicator.utils.CriteriaUtils;
import com.soprasteria.g4it.backend.client.gen.connector.apiexposition.dto.ModeRest;
import com.soprasteria.g4it.backend.exception.NumEcoEvalConnectorRuntimeException;
import com.soprasteria.g4it.backend.external.numecoeval.client.CalculationClient;
import com.soprasteria.g4it.backend.external.numecoeval.mapper.NumEcoEvalCalculationReportMapper;
import com.soprasteria.g4it.backend.external.numecoeval.mapper.NumEcoEvalInputReportMapper;
import com.soprasteria.g4it.backend.external.numecoeval.modeldb.NumEcoEvalCalculationReport;
import com.soprasteria.g4it.backend.external.numecoeval.repository.NumEcoEvalCalculationReportRepository;
import com.soprasteria.g4it.backend.external.numecoeval.repository.NumEcoEvalInputReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Remoting service.
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class NumEcoEvalRemotingService {

    /**
     * CalculationClient numEcoEval webClient
     */
    private final CalculationClient calculationClient;

    /**
     * Repository to save report.
     */
    private final NumEcoEvalCalculationReportRepository numEcoEvalCalculationReportRepository;

    /**
     * Repository to save report.
     */
    private final NumEcoEvalInputReportRepository numEcoEvalInputReportRepository;

    /**
     * Calculation Mapper.
     */
    private final NumEcoEvalCalculationReportMapper numEcoEvalCalculationReportMapper;

    /**
     * Input Data Exposition Mapper.
     */
    private final NumEcoEvalInputReportMapper numEcoEvalInputReportMapper;

    /**
     * Call InputDataExposition in numEcoEval.
     *
     * @param dataCenterCsv        the datacenter csv file.
     * @param physicalEquipmentCsv the physical equipment csv file.
     * @param virtualEquipmentCsv  the virtual equipment csv file.
     * @param applicationCsv       the application csv file.
     * @param organizationId       the organization id as string.
     * @param batchDate            the batch date.
     * @param batchName            the batch name.
     * @throws NumEcoEvalConnectorRuntimeException when no csv file exist.
     */
    public void callInputDataExposition(final Resource dataCenterCsv, final Resource physicalEquipmentCsv,
                                        final Resource virtualEquipmentCsv, final Resource applicationCsv,
                                        final String organizationId, final String batchDate, final String batchName) throws NumEcoEvalConnectorRuntimeException {

        if (dataCenterCsv != null && dataCenterCsv.exists() ||
                physicalEquipmentCsv != null && physicalEquipmentCsv.exists() ||
                virtualEquipmentCsv != null && virtualEquipmentCsv.exists() ||
                applicationCsv != null && applicationCsv.exists()) {

            final var batchDateAsLocalDate = Optional.ofNullable(batchDate).map(LocalDate::parse).orElse(null);

            final var rapportImportRest = calculationClient.importCSV(batchName, organizationId, batchDateAsLocalDate,
                    getFile(dataCenterCsv), getFile(physicalEquipmentCsv), getFile(virtualEquipmentCsv), getFile(applicationCsv));

            // Save report.
            numEcoEvalInputReportRepository.saveAll(numEcoEvalInputReportMapper.toEntities(rapportImportRest, batchName));
        } else {
            throw new NumEcoEvalConnectorRuntimeException("No file generated");
        }
    }

    /**
     * Call NumEcoEval calculation API with default mode ASYNC
     *
     * @param batchName the batch name.
     */
    public void callCalculation(final String batchName, final List<String> criteriaList) {
        callCalculation(batchName, ModeRest.ASYNC, criteriaList);
    }

    /**
     * Call NumEcoEval calculation API
     *
     * @param batchName the batch name.
     * @param modeRest  the mode SYNC or ASYNC
     */
    public void callCalculation(final String batchName, final ModeRest modeRest, final List<String> criteriaKeyList) {
        List<String> criteriaList = Optional.ofNullable(criteriaKeyList)
                .map(keys -> keys.stream()
                        .map(CriteriaUtils::transformCriteriaKeyToCriteriaName)
                        .toList())
                .orElseGet(() -> Constants.CRITERIA_LIST.stream()
                        .map(CriteriaUtils::transformCriteriaKeyToCriteriaName)
                        .toList());

        final var rapportDemandeCalculRest = calculationClient.submitCalculations(batchName, modeRest, criteriaList);

        final NumEcoEvalCalculationReport entityReport = numEcoEvalCalculationReportMapper.toEntity(rapportDemandeCalculRest);
        //As NumEcoEval API always return nomLot : null we set it until fix on NumEcoEval side
        entityReport.setBatchName(batchName);
        numEcoEvalCalculationReportRepository.save(entityReport);

    }

    /**
     * Utility function to get a File from a Resource
     *
     * @param resource the resource
     * @return the File
     */
    private File getFile(final Resource resource) {
        if (resource != null && resource.exists()) {
            try {
                return resource.getFile();
            } catch (final IOException e) {
                log.error("Error occurs during file sending: {}", e.getMessage());
            }
        }
        return null;
    }

    /**
     * @param batchName      the batch name
     * @param organizationId the organizationId as string
     * @return the String
     */
    public String getCalculationsProgress(final String batchName, final String organizationId) {
        try {
            final var statutCalculRest = calculationClient.getCalculationsStatus(batchName, organizationId);
            return statutCalculRest.getEtat();
        } catch (Exception e) {
            log.error("Error occurs during fetching of calculation status: {}", e.getMessage());
        }
        return null;

    }

}
