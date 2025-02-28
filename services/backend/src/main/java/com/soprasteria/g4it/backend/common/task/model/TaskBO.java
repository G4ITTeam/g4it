/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.common.task.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
public class TaskBO {

    private Long id;
    private String status;
    private String type;
    private String progressPercentage;
    private List<String> details;
    private List<String> errors;
    private List<String> criteria;
    private LocalDateTime creationDate;
    private LocalDateTime lastUpdateDate;
}
