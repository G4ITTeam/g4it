/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiinventory.mapper;

import com.soprasteria.g4it.backend.apibatchexport.modeldb.ExportReport;
import com.soprasteria.g4it.backend.apiinventory.model.InventoryBO;
import com.soprasteria.g4it.backend.apiinventory.model.InventoryExportReportBO;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import com.soprasteria.g4it.backend.common.task.model.TaskBO;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

/**
 * Inventory mapper.
 */
@Mapper(componentModel = "spring")
public interface InventoryMapper {

    /**
     * Map to business object.
     *
     * @param source entity.
     * @return the business object.
     */
    @Mapping(target = "organization", source = "organization.name")
    @Mapping(target = "organizationId", source = "organization.id")
    @Mapping(target = "organizationStatus", source = "organization.status")
    InventoryBO toBusinessObject(final Inventory source);

    /**
     * Map to business object list.
     *
     * @param source entity list.
     * @return the business object list.
     */
    List<InventoryBO> toBusinessObject(final List<Inventory> source);

    /**
     * Map to entity.
     *
     * @param organization the organization.
     * @param name         the inventory name.
     * @param type         the inventory type.
     * @return entity.
     */
    @Mapping(target = "organization", source = "organization")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "isNewArch", source = "isNewArch")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "lastUpdateDate", ignore = true)
    Inventory toEntity(final Organization organization, final String name, final String type, final Boolean isNewArch);

    @Mapping(target = "organization", ignore = true)
    Inventory toEntity(InventoryBO inventoryBO);

    /**
     * Map to business object.
     *
     * @param source entity list.
     * @return the business object.
     */
    @Named("createInventoryMapping")
    @Mapping(target = "organization", source = "organization.name")
    @Mapping(target = "dataCenterCount", ignore = true)
    @Mapping(target = "physicalEquipmentCount", ignore = true)
    @Mapping(target = "virtualEquipmentCount", ignore = true)
    @Mapping(target = "applicationCount", ignore = true)
    @Mapping(target = "integrationReports", ignore = true)
    @Mapping(target = "evaluationReports", ignore = true)
    @Mapping(target = "exportReport", ignore = true)
    InventoryBO toCreateBusinessObject(final Inventory source);

    @Mapping(target = "batchStatusCode", source = "statusCode")
    @Mapping(target = "resultFileSize", source = "exportFileSize")
    @Mapping(target = "resultFileUrl", source = "exportFilename")
    @Mapping(target = "createTime", source = "batchCreateTime")
    @Mapping(target = "endTime", source = "batchEndTime")
    InventoryExportReportBO toBusinessObject(ExportReport exportReport);

    TaskBO toBusinessObject(Task task);
}
