/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apiinventory.mapper;

import com.soprasteria.g4it.backend.apiinventory.model.InventoryBO;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import org.mapstruct.IterableMapping;
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
    InventoryBO toBusinessObject(final Inventory source);

    /**
     * Map to business object list.
     *
     * @param source entity list.
     * @return the business object list.
     */
    List<InventoryBO> toBusinessObject(final List<Inventory> source);

    /**
     * Map to business object without count.
     *
     * @param source entity.
     * @return the business object.
     */


    @Named("lightMapping")
    @Mapping(target = "organization", source = "organization.name")
    @Mapping(target = "dataCenterCount", ignore = true)
    @Mapping(target = "physicalEquipmentCount", ignore = true)
    @Mapping(target = "virtualEquipmentCount", ignore = true)
    @Mapping(target = "applicationCount", ignore = true)
    @Mapping(target = "integrationReports", ignore = true)
    InventoryBO toLightBusinessObject(final Inventory source);

    /**
     * Map to business object list.
     *
     * @param source entity list.
     * @return the business object list.
     */
    @IterableMapping(qualifiedByName = "lightMapping")
    List<InventoryBO> toLightBusinessObject(final List<Inventory> source);

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
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "lastUpdateDate", ignore = true)
    Inventory toEntity(final Organization organization, final String name, final String type);


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
    InventoryBO toCreateBusinessObject(final Inventory source);

}
