/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.external.numecoeval.mapper;

import com.soprasteria.g4it.backend.client.gen.connector.apiexposition.dto.RapportImportRest;
import com.soprasteria.g4it.backend.external.numecoeval.modeldb.NumEcoEvalInputReport;
import org.mapstruct.Builder;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * NumEcoEvalInputReport / RapportImportRest Mapper.
 */
@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface NumEcoEvalInputReportMapper {

    /**
     * Mapping from RapportImportRest to NumEcoEvalInputReport.
     *
     * @param input     object from numEcoEval.
     * @param batchName the unique identifier in numEcoEval for data inputs.
     * @return entity
     */
    @Mapping(target = "batchName", source = "batchName")
    @Mapping(target = "file", source = "input.fichier")
    @Mapping(target = "errors", source = "input.erreurs")
    @Mapping(target = "importLinesNumber", source = "input.nbrLignesImportees")
    NumEcoEvalInputReport toEntity(RapportImportRest input, final String batchName);

    /**
     * Mapping from business objects to entities.
     *
     * @param input     business object list.
     * @param batchName the unique identifier in numEcoEval for data inputs.
     * @return entities
     */
    List<NumEcoEvalInputReport> toEntities(final List<RapportImportRest> input, @Context final String batchName);

}
