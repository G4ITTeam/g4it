/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.external.numecoeval.business;

import com.google.common.math.Quantiles;
import com.soprasteria.g4it.backend.client.gen.connector.apireferentiel.dto.*;
import com.soprasteria.g4it.backend.external.numecoeval.client.ReferentialClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Referential Remoting Service.
 */
@Service
@Slf4j
public class NumEcoEvalReferentialRemotingService {

    /**
     * ReferentialClient
     */
    @Autowired
    private ReferentialClient referentialClient;


    /**
     * Get NumEcoEval country.
     *
     * @return country list (string).
     */
    @Cacheable("getCountryList")
    public List<String> getCountryList() {
        return referentialClient.getMixElec().stream()
                .filter(Objects::nonNull)
                .map(MixElectriqueDTO::getPays)
                .distinct()
                .toList();
    }

    /**
     * Get electricity mix map quartiles : ([country, criteria], quartile)
     *
     * @return the map of quartiles
     */
    @Cacheable("getElectricityMixQuartiles")
    public Map<Pair<String, String>, Integer> getElectricityMixQuartiles() {
        var allMixElec = referentialClient.getMixElec().stream()
                .filter(mix -> {
                    var hasNullValue = mix.getCritere() == null || mix.getValeur() == null;
                    if (hasNullValue) {
                        log.error("Electricity mix of country: {} has null criteria or value", mix.getPays());
                    }
                    return !hasNullValue;
                }).toList();

        var quartileValues = allMixElec.stream()
                .collect(Collectors.groupingBy(MixElectriqueDTO::getCritere))
                .entrySet().stream().collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> Quantiles
                                .quartiles()
                                .indexes(1, 2, 3, 4)
                                .compute(entry.getValue().stream().map(MixElectriqueDTO::getValeur).sorted().toList())));

        return allMixElec.stream().collect(Collectors.toMap(
                mix -> Pair.of(mix.getPays(), mix.getCritere()),
                mix -> {

                    int quartileIndex;
                    if (mix.getValeur() == null) {
                        log.error("Electricity mix of country: {} and criteria: {} has null value", mix.getPays(), mix.getCritere());
                        quartileIndex = 4;
                    } else if (mix.getValeur() <= quartileValues.get(mix.getCritere()).get(1)) {
                        quartileIndex = 1;
                    } else if (mix.getValeur() <= quartileValues.get(mix.getCritere()).get(2)) {
                        quartileIndex = 2;
                    } else if (mix.getValeur() <= quartileValues.get(mix.getCritere()).get(3)) {
                        quartileIndex = 3;
                    } else {
                        quartileIndex = 4;
                    }
                    return quartileIndex;
                }));
    }

    /**
     * Get NumEcoEval Type Equipement Name.
     *
     * @return equipment type list (string).
     */
    public List<String> getEquipmentTypeList() {
        return referentialClient.getEquipementTypes().stream()
                .filter(Objects::nonNull)
                .map(TypeEquipementDTO::getType)
                .distinct()
                .toList();
    }

    public List<CritereDTO> getCriteriaList() {
        return referentialClient.getAllCriteria();
    }

    public List<EtapeDTO> getLifecycleSteps() {
        return referentialClient.getAllLifecycleSteps();
    }

    public List<TypeItemDTO> getItemTypes() {
        return referentialClient.getAllItemTypes();
    }

}
