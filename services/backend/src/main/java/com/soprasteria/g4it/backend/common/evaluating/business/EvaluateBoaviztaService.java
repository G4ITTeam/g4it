/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.common.evaluating.business;

import com.soprasteria.g4it.backend.apiindicator.modeldb.RefSustainableIndividualPackage;
import com.soprasteria.g4it.backend.apiindicator.repository.RefSustainableIndividualPackageRepository;
import com.soprasteria.g4it.backend.apiindicator.utils.CriteriaUtils;
import com.soprasteria.g4it.backend.apiinout.modeldb.OutVirtualEquipment;
import com.soprasteria.g4it.backend.apiinout.repository.OutVirtualEquipmentRepository;
import com.soprasteria.g4it.backend.client.gen.connector.apireferentiel.dto.EtapeDTO;
import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.exception.AsyncTaskException;
import com.soprasteria.g4it.backend.external.numecoeval.business.NumEcoEvalReferentialRemotingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EvaluateBoaviztaService {

    @Value("${local.working.folder}")
    private String localWorkingFolder;

    @Autowired
    private RefSustainableIndividualPackageRepository refSustainableIndividualPackageRepository;

    @Autowired
    private OutVirtualEquipmentRepository outVirtualEquipmentRepository;

    @Autowired
    private EvaluateCloudService evaluateCloudService;

    @Autowired
    NumEcoEvalReferentialRemotingService numEcoEvalReferentialRemotingService;

    private static final List<String> DEFAULT_CRITERIA = List.of(
            "climate-change",
            "ionising-radiation",
            "acidification",
            "particulate-matter",
            "resource-use"
    );

    /**
     * Do evaluate cloud instances with boaviztapi
     *
     * @param context the context
     * @param task    the task
     */
    public void doEvaluate(final Context context, final Task task) {

        final long start = System.currentTimeMillis();
        final Long taskId = task.getId();

        Path exportDirectory = Path.of(localWorkingFolder).resolve("export").resolve(String.valueOf(taskId));

        try {
            Files.createDirectories(exportDirectory);
        } catch (IOException e) {
            throw new AsyncTaskException("Cannot create export directory", e);
        }

        List<String> lifecycleSteps = numEcoEvalReferentialRemotingService.getLifecycleSteps().stream()
                .map(EtapeDTO::getCode)
                .toList();

        List<String> activeCriteria = task.getCriteria() == null ?
                DEFAULT_CRITERIA :
                task.getCriteria().stream().filter(CriteriaUtils.CRITERIA_MAP::containsKey).toList();

        Map<String, Double> refSipByCriteria = refSustainableIndividualPackageRepository.findAll()
                .stream()
                .filter(item -> activeCriteria.contains(CriteriaUtils.transformCriteriaNameToCriteriaKey(item.getCriteria())))
                .collect(Collectors.toMap(
                        r -> CriteriaUtils.transformCriteriaNameToCriteriaKey(r.getCriteria()),
                        RefSustainableIndividualPackage::getIndividualSustainablePackage)
                );

        List<OutVirtualEquipment> outVirtualEquipments = evaluateCloudService.evaluateCloudInstances(context, task, exportDirectory,
                activeCriteria, lifecycleSteps, refSipByCriteria);

        outVirtualEquipmentRepository.saveAll(outVirtualEquipments);

        log.info("End evaluating impacts for {} in {}s and key size: {}", context.log(),
                (System.currentTimeMillis() - start) / 1000,
                outVirtualEquipments.size());

    }

}
