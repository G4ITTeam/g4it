/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.mapper;

import com.soprasteria.g4it.backend.server.gen.api.dto.InApplicationRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.InDatacenterRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.InPhysicalEquipmentRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.InVirtualEquipmentRest;
import org.apache.commons.csv.CSVRecord;
import org.mapstruct.Mapper;

import java.time.LocalDateTime;
import java.util.List;

import static com.soprasteria.g4it.backend.common.utils.CsvUtils.*;

@Mapper(componentModel = "spring")
public interface CsvToInMapper {

    /**
     * From Csv import to Rest
     */
    default InDatacenterRest csvInDatacenterToRest(CSVRecord csvRecord, final Long inventoryId) {
        return InDatacenterRest.builder()
                .name(read(csvRecord, "nomCourtDatacenter"))
                .inventoryId(inventoryId)
                .fullName(read(csvRecord, "nomLongDatacenter"))
                .pue(readDouble(csvRecord, "pue"))
                .location(read(csvRecord, "localisation"))
                .creationDate(LocalDateTime.now())
                .commonFilters(List.of(read(csvRecord, "nomEntite", "")))
                .build();
    }

    default InPhysicalEquipmentRest csvInPhysicalEquipmentToRest(CSVRecord csvRecord, final Long inventoryId) {
        return InPhysicalEquipmentRest.builder()
                .name(read(csvRecord, "nomEquipementPhysique"))
                .inventoryId(inventoryId)
                .datacenterName(read(csvRecord, "nomCourtDatacenter"))
                .location(read(csvRecord, "paysDUtilisation"))
                .quantity(readDouble(csvRecord, "quantite", 1d))
                .type(read(csvRecord, "type"))
                .model(read(csvRecord, "modele"))
                .datePurchase(readLocalDate(csvRecord, "dateAchat"))
                .dateWithdrawal(readLocalDate(csvRecord, "dateRetrait"))
                .source(read(csvRecord, "nomSourceDonnee"))
                .cpuCoreNumber(readDouble(csvRecord, "nbCoeur"))
                .electricityConsumption(readDouble(csvRecord, "consoElecAnnuelle"))
                .creationDate(LocalDateTime.now())
                .filters(List.of(read(csvRecord, "statut", "")))
                .commonFilters(List.of(read(csvRecord, "nomEntite", "")))
                .build();
    }

    default InVirtualEquipmentRest csvInVirtualEquipmentToRest(CSVRecord csvRecord, final Long inventoryId) {
        return InVirtualEquipmentRest.builder()
                .name(read(csvRecord, "nomEquipementVirtuel"))
                .inventoryId(inventoryId)
                .physicalEquipmentName(read(csvRecord, "nomEquipementPhysique"))
                .quantity(readDouble(csvRecord, "quantite"))
                .type(read(csvRecord, "typeEqv"))
                .vcpuCoreNumber(readDouble(csvRecord, "vCPU"))
                .allocationFactor(readDouble(csvRecord, "cleRepartition"))
                .electricityConsumption(readDouble(csvRecord, "consoElecAnnuelle"))
                .quantity(1d)
                .infrastructureType("ON_PREMISE")
                .creationDate(LocalDateTime.now())
                .filters(List.of(read(csvRecord, "cluster", "")))
                .commonFilters(List.of(read(csvRecord, "nomEntite", "")))
                .build();
    }

    default InApplicationRest csvInApplicationToRest(CSVRecord csvRecord, final Long inventoryId) {
        return InApplicationRest.builder()
                .name(read(csvRecord, "nomApplication"))
                .inventoryId(inventoryId)
                .environment(read(csvRecord, "typeEnvironnement"))
                .virtualEquipmentName(read(csvRecord, "nomEquipementVirtuel"))
                .physicalEquipmentName(read(csvRecord, "nomEquipementPhysique"))
                .creationDate(LocalDateTime.now())
                .filters(List.of(
                        read(csvRecord, "domaine", ""),
                        read(csvRecord, "sousDomaine", "")
                ))
                .commonFilters(List.of(read(csvRecord, "nomEntite", "")))
                .build();
    }
}
