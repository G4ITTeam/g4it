/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.external.numecoeval.business;

import com.google.common.math.Quantiles;
import com.soprasteria.g4it.backend.client.gen.connector.apireferentiel.dto.MixElectriqueDTO;
import com.soprasteria.g4it.backend.client.gen.connector.apireferentiel.dto.TypeEquipementDTO;
import com.soprasteria.g4it.backend.external.numecoeval.client.ReferentialClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Referential Remoting Service.
 */
@Service
@AllArgsConstructor
@Slf4j
public class NumEcoEvalReferentialRemotingService {

    /**
     * ReferentialClient
     */
    private ReferentialClient referentialClient;

    /**
     * Map containing max double value and associated quartile number.
     */
    private Map<String, Map<Integer, Double>> quartileValues;

    /**
     * Map containing <country/criteria> in key and quartile index in value.
     */
    private Map<Pair<String, String>, Integer> mixElecWithQuartile;

    /**
     * Set containing the criteria.
     */
    @Value("${g4it.criteria}")
    private Set<String> criterias;

    /**
     * Map containing index in key and calculated value for index in value.
     */
    private Map<Integer, Double> computeSum;

    /**
     * Map containing country in key and sum of all the impacts  in value.
     */
    private Map<String, Integer> countrySumImpactMap;

    /**
     * Get NumEcoEval country.
     *
     * @return country list (string).
     */
    public List<String> getCountryList() {
        return referentialClient.getMixElec().stream()
                .filter(Objects::nonNull)
                .map(MixElectriqueDTO::getPays)
                .distinct()
                .toList();
    }

    /**
     * Get quartile index.
     *
     * @param criteria mix elec criteria.
     * @param country  mix elec country.
     * @return the quartile index.
     */
    public Integer getMixElecQuartileIndex(final String criteria, final String country) {
        if (this.mixElecWithQuartile == null || this.mixElecWithQuartile.isEmpty()) {
            final var mixElecs = referentialClient.getMixElec();
            this.mixElecWithQuartile = mixElecs.stream().collect(Collectors.toMap(
                    mix -> Pair.of(mix.getPays(), mix.getCritere()),
                    this::calculateQuartileIndex));
        }
        return mixElecWithQuartile.get(Pair.of(country, criteria));
    }

    /**
     * For a country, estimate the environment impact regarding electricity mix and returns true if the country has a low impact
     * A low impact means:
     * - getting all countries quartile position for each criterias and sum them
     * - checking if the country is in the first quartile of these countries
     *
     * @param country the country.
     * @return if country has a low impact.
     */
    public boolean isLowImpact(final String country) {
        if (this.countrySumImpactMap == null || this.countrySumImpactMap.isEmpty()) {
            this.countrySumImpactMap = this.getCountryList().stream()
                    .collect(Collectors.toMap(
                            refCountry -> refCountry,
                            refCountry -> criterias.stream()
                                    .mapToInt(criteria -> getMixElecQuartileIndex(criteria, refCountry))
                                    .filter(Objects::nonNull)
                                    .sum()
                    ));

            this.computeSum = Quantiles
                    .quartiles()
                    .indexes(1, 2, 3, 4)
                    .compute(countrySumImpactMap.values());
        }

        int firstQuartileValue = computeSum.get(1).intValue();
        Integer totalImpact = this.countrySumImpactMap.get(country);

        return totalImpact != null && totalImpact < firstQuartileValue;
    }

    /**
     * Calculate quartile index for each mixElec.
     *
     * @param mix the mix elec on which to calculate the quartile index.
     * @return the quartile index.
     */
    private Integer calculateQuartileIndex(final MixElectriqueDTO mix) {
        if (this.quartileValues == null || this.quartileValues.isEmpty()) {
            this.quartileValues = referentialClient.getMixElec().stream().collect(Collectors.groupingBy(MixElectriqueDTO::getCritere))
                    .entrySet().stream().collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> Quantiles
                                    .quartiles()
                                    .indexes(1, 2, 3, 4)
                                    .compute(entry.getValue().stream().map(MixElectriqueDTO::getValeur).sorted().toList())
                    ));
        }
        int quartileIndex;
        if (mix.getValeur() <= quartileValues.get(mix.getCritere()).get(1)) {
            quartileIndex = 1;
        } else if (mix.getValeur() <= quartileValues.get(mix.getCritere()).get(2)) {
            quartileIndex = 2;
        } else if (mix.getValeur() <= quartileValues.get(mix.getCritere()).get(3)) {
            quartileIndex = 3;
        } else {
            quartileIndex = 4;
        }
        return quartileIndex;
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

}
