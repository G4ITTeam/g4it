/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.common.evaluating.business;

import com.soprasteria.g4it.backend.apiinout.modeldb.InVirtualEquipment;
import com.soprasteria.g4it.backend.apiinout.modeldb.OutVirtualEquipment;
import com.soprasteria.g4it.backend.apiinout.repository.InVirtualEquipmentRepository;
import com.soprasteria.g4it.backend.common.evaluating.mapper.AggregationToOut;
import com.soprasteria.g4it.backend.common.evaluating.mapper.ImpactToCsvRecord;
import com.soprasteria.g4it.backend.common.evaluating.mapper.InputToCsvRecord;
import com.soprasteria.g4it.backend.common.evaluating.model.AggValuesBO;
import com.soprasteria.g4it.backend.common.filesystem.model.CsvFileMapperInfo;
import com.soprasteria.g4it.backend.common.filesystem.model.FileType;
import com.soprasteria.g4it.backend.common.filesystem.model.Header;
import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.common.utils.CsvUtils;
import com.soprasteria.g4it.backend.exception.AsyncTaskException;
import com.soprasteria.g4it.backend.external.boavizta.business.BoaviztapiService;
import com.soprasteria.g4it.backend.external.boavizta.model.response.BoaImpactRest;
import com.soprasteria.g4it.backend.external.boavizta.model.response.BoaResponseRest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class EvaluateCloudService {

    @Autowired
    InVirtualEquipmentRepository inVirtualEquipmentRepository;

    @Autowired
    CsvFileMapperInfo csvFileMapperInfo;

    @Autowired
    TaskRepository taskRepository;

    @Autowired
    BoaviztapiService boaviztapiService;

    @Autowired
    ImpactToCsvRecord impactToCsvRecord;

    @Autowired
    InputToCsvRecord inputToCsvRecord;

    @Autowired
    AggregationToOut aggregationToOut;


    /**
     * Evaluate cloud instances
     * Return the list of output virtual equipments
     * Print csv files in export directory
     *
     * @param context          the context
     * @param task             the task
     * @param exportDirectory  the export directory
     * @param criteria         the list of criterion
     * @param refSipByCriteria the map of criteria, sip
     * @return the list of output virtual equipments
     */
    public List<OutVirtualEquipment> evaluateCloudInstances(Context context, Task task, Path exportDirectory, List<String> criteria,
                                                            List<String> lifecycleSteps, Map<String, Double> refSipByCriteria) {

        Map<String, AggValuesBO> aggregationVirtualEquipments = new HashMap<>();

        String digitalServiceUid = task.getDigitalServiceUid();

        long totalVirtualEquipments = inVirtualEquipmentRepository.countByDigitalServiceUid(digitalServiceUid);

        try (CSVPrinter csvInVirtualEquipment = getCsvPrinter(FileType.VIRTUAL_EQUIPMENT_CLOUD_INSTANCE, exportDirectory);
             CSVPrinter csvVirtualEquipment = getCsvPrinter(FileType.VIRTUAL_EQUIPMENT_INDICATOR_CLOUD_INSTANCE, exportDirectory)) {

            int pageNumber = 0;
            while (true) {
                Pageable page = PageRequest.of(pageNumber, Constants.BATCH_SIZE, Sort.by("name"));
                final List<InVirtualEquipment> virtualEquipments = inVirtualEquipmentRepository.findByDigitalServiceUid(digitalServiceUid, page);

                if (virtualEquipments.isEmpty()) {
                    break;
                }

                log.info("Evaluating {} virtual equipments, page {}/{}", virtualEquipments.size(), pageNumber + 1, (int) Math.ceil((double) totalVirtualEquipments / Constants.BATCH_SIZE));

                for (InVirtualEquipment virtualEquipment : virtualEquipments) {

                    csvInVirtualEquipment.printRecord(inputToCsvRecord.toCsv(context.getDigitalServiceName(), virtualEquipment));

                    BoaResponseRest response = boaviztapiService.runBoaviztCalculations(virtualEquipment);

                    var criteriaImpactMap = response == null ?
                            new HashMap<String, BoaImpactRest>() :
                            Map.of("climate-change", response.getImpacts().getGwp());

                    for (String criterion : criteria) {
                        BoaImpactRest impact = null;
                        if (criteriaImpactMap.containsKey(criterion)) {
                            impact = criteriaImpactMap.get(criterion);
                        }
                        Double sipValue = refSipByCriteria.get(criterion);

                        for (String lifecycleStep : lifecycleSteps) {

                            Double unitImpact = null;
                            if (impact != null) {
                                unitImpact = switch (lifecycleStep) {
                                    case Constants.FABRICATION -> impact.getEmbedded().getValue();
                                    case Constants.UTILISATION -> impact.getUse().getValue();
                                    default -> null;
                                };
                            }

                            AggValuesBO values = createAggValuesBO(virtualEquipment.getQuantity(),
                                    virtualEquipment.getElectricityConsumption(), unitImpact, sipValue,
                                    virtualEquipment.getDurationHour(), virtualEquipment.getWorkload());

                            String unit = unitImpact == null ? null : impact.getUnit();
                            String indicatorStatus = unitImpact == null ? "ERROR" : "OK";

                            aggregationVirtualEquipments
                                    .computeIfAbsent(
                                            aggregationToOut.keyVirtualEquipment(virtualEquipment,
                                                    criterion, lifecycleStep, indicatorStatus, unit),
                                            k -> new AggValuesBO())
                                    .add(values);

                            csvVirtualEquipment.printRecord(impactToCsvRecord.toCsv(context, task.getId(), virtualEquipment,
                                    values, criterion, lifecycleStep, indicatorStatus, unit));

                        }
                    }
                }

                final long currentTotal = (long) Constants.BATCH_SIZE * pageNumber + virtualEquipments.size();

                // set progress percentage, 0% to 90% is for this process, 90% to 100% is for compressing exports
                double processFactor = 0.9;
                task.setProgressPercentage((int) Math.ceil(currentTotal * 100L * processFactor / totalVirtualEquipments) + "%");
                task.setLastUpdateDate(LocalDateTime.now());
                taskRepository.save(task);

                pageNumber++;
                virtualEquipments.clear();
            }

        } catch (IOException e) {
            log.error("Cannot write csv output files", e);
            throw new AsyncTaskException("An error occurred on writing csv files", e);
        }

        return aggregationVirtualEquipments.entrySet().stream()
                .map(entry -> aggregationToOut.mapVirtualEquipment(entry.getKey(), entry.getValue(), task.getId(), refSipByCriteria))
                .toList();
    }

    /**
     * Get csv printer for a fileType and output in directory
     *
     * @param fileType  the fileType
     * @param directory the output directory
     * @return the csv printer
     * @throws IOException local file creation exception
     */
    private CSVPrinter getCsvPrinter(FileType fileType, Path directory) throws IOException {

        return new CSVPrinter(new FileWriter(
                directory.resolve(fileType.getFileName() + Constants.CSV).toFile()
        ), CSVFormat.Builder.create()
                .setHeader(csvFileMapperInfo.getMapping(fileType).stream()
                        .map(Header::getName).toArray(String[]::new))
                .setDelimiter(CsvUtils.DELIMITER)
                .build());
    }


    /**
     * Create aggValuesBO  with default values
     *
     * @param quantity        the quantity
     * @param elecConsumption the elec consumption
     * @param unitImpact      the unit impact
     * @param sipValue        the sip value
     * @param usageDuration   the usage duration
     * @param workload        the workload
     * @return aggValuesBO object
     */
    private AggValuesBO createAggValuesBO(Double quantity,
                                          Double elecConsumption,
                                          Double unitImpact,
                                          Double sipValue,
                                          Double usageDuration,
                                          Double workload) {
        Double localQuantity = quantity == null ? 1d : quantity;
        Double impact = unitImpact == null ? 0d : unitImpact * localQuantity;

        return AggValuesBO.builder()
                .countValue(1L)
                .unitImpact(impact)
                .peopleEqImpact(sipValue == null ? 0d : impact / sipValue)
                .electricityConsumption(elecConsumption == null ? 0d : elecConsumption)
                .quantity(localQuantity)
                .workload(workload == null ? 0d : workload)
                .usageDuration(usageDuration == null ? 0d : usageDuration)
                .errors(new HashSet<>())
                .lifespan(0d)
                .build();
    }

}
