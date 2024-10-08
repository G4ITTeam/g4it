/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.schedulerlocked;

import com.soprasteria.g4it.backend.apibatchevaluation.business.InventoryEvaluationService;
import com.soprasteria.g4it.backend.apibatchevaluation.repository.InventoryEvaluationReportRepository;
import com.soprasteria.g4it.backend.common.utils.EvaluationBatchStatus;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.batch.core.BatchStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Profile("!test")
@Slf4j
public class InventoryEvaluationScheduler {

    @Autowired
    private InventoryEvaluationService inventoryEvaluationService;

    @Autowired
    private InventoryEvaluationReportRepository inventoryEvaluationReportRepository;

    /**
     * On start-up of the component:
     * - verify if there is old evaluations to aggregate
     * - optimization with obsolete evaluations is done by skipping their aggregation
     */
    @Transactional
    @EventListener(ApplicationReadyEvent.class)
    public void aggregateOldData() {
        var evaluationsSortedByCreateTimeDesc = inventoryEvaluationReportRepository.findByBatchStatusCodeAndIsApplicationAggregated(
                BatchStatus.COMPLETED.name(), false, Sort.by(Sort.Direction.DESC, "inventoryId", "createTime")
        );

        if (evaluationsSortedByCreateTimeDesc.isEmpty()) return;

        // optimize by skipping evaluations that has no application item
        List<Long> reportIdsWithoutApplication = new ArrayList<>();

        for (var evaluation : evaluationsSortedByCreateTimeDesc) {
            var inventory = evaluation.getInventory();

            if (inventory.getApplicationCount() == null || inventory.getApplicationCount() == 0) {
                reportIdsWithoutApplication.add(evaluation.getId());
            }
        }

        int nbToSkipAggregation = inventoryEvaluationReportRepository.updateIsApplicationAggregated(reportIdsWithoutApplication);
        int nbToAggregated = inventoryEvaluationReportRepository.resetReportsIfNotAggregated(EvaluationBatchStatus.AGGREGATION_IN_PROGRESS.name());

        log.info("Found {} evaluations to skip aggregation, and {} evaluations to aggregate", nbToSkipAggregation, nbToAggregated);

    }

    @Scheduled(fixedDelay = 10_000)
    @SchedulerLock(name = "calculateProgressPercentage", lockAtMostFor = "2m", lockAtLeastFor = "9s")
    public void calculateProgressPercentage() {
        inventoryEvaluationService.calculateProgressPercentage();
    }

    @Scheduled(fixedDelay = 10_000, initialDelay = 5000)
    @SchedulerLock(name = "aggregateIndicatorsData", lockAtMostFor = "2m", lockAtLeastFor = "9s")
    public void aggregateIndicatorsData() {
        inventoryEvaluationService.aggregateIndicatorsData();
    }
}
