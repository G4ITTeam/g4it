/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apireferential.business;

import com.soprasteria.g4it.backend.apireferential.model.AnalysisTableBO;
import com.soprasteria.g4it.backend.apireferential.modeldb.Criterion;
import com.soprasteria.g4it.backend.apireferential.modeldb.ItemImpact;
import com.soprasteria.g4it.backend.apireferential.modeldb.LifecycleStep;
import com.soprasteria.g4it.backend.apireferential.modeldb.MatchingItem;
import com.soprasteria.g4it.backend.apireferential.repository.CriterionRepository;
import com.soprasteria.g4it.backend.apireferential.repository.ItemImpactRepository;
import com.soprasteria.g4it.backend.apireferential.repository.LifecycleStepRepository;
import com.soprasteria.g4it.backend.apireferential.repository.MatchingItemRepository;
import com.soprasteria.g4it.backend.common.utils.StringUtils;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Referential  analysis get service
 */
@Service
@AllArgsConstructor
public class ReferentialGetAnalysisService {

    @Autowired
    private ItemImpactRepository itemImpactRepo;

    @Autowired
    private MatchingItemRepository matchingItemRepo;

    @Autowired
    private CriterionRepository criterionRepo;

    @Autowired
    private LifecycleStepRepository lifecycleStepRepo;

    /**
     * Retrieves analysis results
     *
     * @return List of AnalysisTableBO.
     */
    public List<AnalysisTableBO> getAnalysis() {
        List<AnalysisTableBO> analysisResults = new ArrayList<>();

        // Validate ref_item_impact
        List<String> errors = validateItemImpacts();
        if (!errors.isEmpty()) {
            analysisResults.add(AnalysisTableBO.builder()
                    .table("ref_item_impact")
                    .errors(errors)
                    .warnings(Collections.emptyList())
                    .build());
        }

        // Validate ref_matching_item
        List<String> warnings = validateMatchingItem();
        if (!warnings.isEmpty()) {
            analysisResults.add(AnalysisTableBO.builder()
                    .table("ref_matching_item")
                    .errors(Collections.emptyList())
                    .warnings(warnings)
                    .build());
        }
        return analysisResults;
    }

    /**
     * Validate ref_item_impact table
     *
     * @return list of errors
     */
    private List<String> validateItemImpacts() {

        Map<String, List<ItemImpact>> impactsByName = itemImpactRepo.findByLevel("2-Equipement").stream()
                .collect(Collectors.groupingBy(ItemImpact::getName));

        List<String> criteriaList = criterionRepo.findAll().stream()
                .map(Criterion::getCode)
                .toList();

        List<String> lifecycleSteps = lifecycleStepRepo.findAll().stream()
                .map(LifecycleStep::getCode)
                .toList();

        List<String> errors = new ArrayList<>();

        // verify if there is any missing  lifecycle-criteria pair when level = '2-Equipement'
        errors.addAll(checkMissingLinesForEquipmentLevel(criteriaList, lifecycleSteps, impactsByName));

        // verify there is no missing criteria  when category = 'electricity-mix'
        errors.addAll(checkMissingCriteriaForCategoryElectricityMix(criteriaList, itemImpactRepo.findByCategory("electricity-mix")));

        // verify missing avg_electricity_consumption when level = '2-Equipement'
        errors.addAll(checkAvgElectricityConsumption(impactsByName));

        return errors;
    }

    /**
     * Checks for missing lifecycle-criteria.
     *
     * @param criteriaList   List of criteria to validate for.
     * @param lifecycleSteps List of lifecycle steps to validate for.
     * @param impactsByName  Map grouping itemImpacts by name.
     * @return list of errors
     */
    private List<String> checkMissingLinesForEquipmentLevel(List<String> criteriaList, List<String> lifecycleSteps,
                                                            Map<String, List<ItemImpact>> impactsByName) {

        return impactsByName.entrySet().stream()
                .map(entry -> {
                    String impactName = entry.getKey();
                    Set<String> presentPairs = entry.getValue().stream()
                            .map(item -> item.getLifecycleStep() + "-" + item.getCriterion())
                            .collect(Collectors.toSet());

                    List<String> missingPairs = new ArrayList<>();

                    for (String lifecycleStep : lifecycleSteps) {
                        for (String criteria : criteriaList) {
                            String pair = lifecycleStep + "-" + criteria;
                            if (!presentPairs.contains(pair)) {
                                missingPairs.add(pair);
                            }
                        }
                    }

                    return missingPairs.isEmpty() ? null
                            : String.format("'%s' is missing '%s' lifecycle-criteria pairs", impactName, String.join(", ", missingPairs));
                })
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Checks for missing criteria.
     *
     * @param criteriaList          List of criteria.
     * @param itemImpactsByCategory List of itemImpacts.
     * @return list of errors
     */
    private List<String> checkMissingCriteriaForCategoryElectricityMix(List<String> criteriaList, List<ItemImpact> itemImpactsByCategory) {
        Map<String, Set<String>> criteriaMap = new HashMap<>();

        for (ItemImpact itemImpact : itemImpactsByCategory) {
            criteriaMap
                    .computeIfAbsent(itemImpact.getName(), k -> new HashSet<>())
                    .add(itemImpact.getCriterion());
        }

        return criteriaMap.entrySet().stream()
                .map(entry -> {
                    String name = entry.getKey();
                    Set<String> existingCriteria = entry.getValue();

                    // Calculate missing criteria
                    long missingCount = criteriaList.stream()
                            .filter(criterion -> !existingCriteria.contains(criterion))
                            .count();

                    return missingCount > 0 ? String.format("'%s' is missing %d criteria", name, missingCount) : null;
                })
                .filter(Objects::nonNull)
                .toList();

    }

    /**
     * Checks for missing average electricity
     *
     * @param impactsByName Map grouping itemImpacts by name.
     * @return list of errors
     */
    private List<String> checkAvgElectricityConsumption(Map<String, List<ItemImpact>> impactsByName) {

        return impactsByName.entrySet().stream()
                .map(entry -> {
                    String refrigerantName = entry.getKey();
                    boolean hasMissingAvgElectricityConsumption = entry.getValue().stream()
                            .anyMatch(itemImpact -> itemImpact.getAvgElectricityConsumption() == null);

                    return hasMissingAvgElectricityConsumption ? String.format("'%s' is missing avg_electricity_consumption", refrigerantName) : null;
                })
                .filter(Objects::nonNull)
                .toList();

    }

    /**
     * Validate ref_matching_item table
     *
     * @return list of warnings
     */
    private List<String> validateMatchingItem() {
        List<String> warnings = new ArrayList<>();
        List<MatchingItem> matchingItems = matchingItemRepo.findAll();
        for (MatchingItem item : matchingItems) {
            if (!StringUtils.isKebabCase(item.getRefItemTarget())) {
                warnings.add("ref_item_target '" + item.getRefItemTarget() + "' has wrong format, should be 'kebab-case-format'.");
            }
        }
        return warnings;
    }

}
