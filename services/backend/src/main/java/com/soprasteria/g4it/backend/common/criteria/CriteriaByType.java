/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.common.criteria;

import java.util.List;


public record CriteriaByType(List<String> active, List<String> subscriber,
                             List<String> organizationIs, List<String> organizationDs,
                             List<String> inventory, List<String> digitalService) {
}
