/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.external.numecoeval.mapper;

import com.soprasteria.g4it.backend.client.gen.connector.apiexposition.dto.RapportDemandeCalculRest;
import com.soprasteria.g4it.backend.external.numecoeval.modeldb.NumEcoEvalCalculationReport;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * NumEcoEvalCalculationReport / RapportDemandeCalculRest Mapper.
 */
@Mapper(componentModel = "spring")
public interface NumEcoEvalCalculationReportMapper {

    /**
     * Mapping from RapportDemandeCalculRest to NumEcoEvalCalculationReport.
     *
     * @param input object from numEcoEval.
     * @return entity
     */
    @Mapping(target = "batchName", source = "input.nomLot")
    @Mapping(target = "datacenterNumber", source = "input.nbrDataCenter")
    @Mapping(target = "physicalEquipmentNumber", source = "input.nbrEquipementPhysique")
    @Mapping(target = "virtualEquipmentNumber", source = "input.nbrEquipementVirtuel")
    @Mapping(target = "applicationNumber", source = "input.nbrApplication")
    @Mapping(target = "messagingNumber", source = "input.nbrMessagerie")
    NumEcoEvalCalculationReport toEntity(final RapportDemandeCalculRest input);

}
