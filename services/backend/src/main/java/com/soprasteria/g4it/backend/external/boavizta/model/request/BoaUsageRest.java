/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.external.boavizta.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class BoaUsageRest {
    @JsonProperty("usage_location")
    private String usageLocation;

    @JsonProperty("use_time_ratio")
    private int useTimeRatio;

    @JsonProperty("hours_life_time")
    private double hoursLifeTime;

    @JsonProperty("time_workload")
    private List<BoaTimeWorkloadRest> timeWorkload;
}
