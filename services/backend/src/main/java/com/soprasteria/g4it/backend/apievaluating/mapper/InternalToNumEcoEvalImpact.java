/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apievaluating.mapper;

import com.soprasteria.g4it.backend.apievaluating.model.ImpactBO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mte.numecoeval.calculs.domain.data.indicateurs.ImpactEquipementVirtuel;

import java.util.List;

@Mapper(componentModel = "spring")
public interface InternalToNumEcoEvalImpact {

    /**
     * From G4IT internal object model to NumEcoEval impact object
     */
    List<ImpactEquipementVirtuel> map(List<ImpactBO> impactBO);

    @Mapping(target = "etapeACV", source = "lifecycleStep")
    @Mapping(target = "critere", source = "criterion")
    @Mapping(target = "statutIndicateur", source = "indicatorStatus")
    @Mapping(target = "unite", source = "unit")
    @Mapping(target = "trace", source = "trace")
    @Mapping(target = "impactUnitaire", source = "unitImpact")
    ImpactEquipementVirtuel map(ImpactBO impactBO);

}