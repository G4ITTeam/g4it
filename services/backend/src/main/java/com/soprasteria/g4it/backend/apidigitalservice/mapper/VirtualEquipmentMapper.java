/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apidigitalservice.mapper;

import com.soprasteria.g4it.backend.apidigitalservice.model.VirtualEquipmentBO;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.VirtualEquipmentCharacteristic;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.VirtualEquipmentDigitalService;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.*;

import java.util.List;
import java.util.Optional;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public abstract class VirtualEquipmentMapper {

    @Mapping(target = "annualOperatingTime", source = "annualUsageTime")
    public abstract VirtualEquipmentBO toBusinessObject(final VirtualEquipmentDigitalService source);

    public abstract List<VirtualEquipmentBO> toBusinessObject(final List<VirtualEquipmentDigitalService> source);

    @Mapping(target = "annualUsageTime", source = "annualOperatingTime")
    public abstract VirtualEquipmentDigitalService toEntity(final VirtualEquipmentBO source, @Context final String type);

    public abstract void merge(@MappingTarget final VirtualEquipmentDigitalService target, final VirtualEquipmentBO source, @Context final String type);

    /**
     * Finalize entity with characteristic.
     *
     * @param source the business object containing input data.
     * @param target the entity to link.
     */
    @AfterMapping
    protected void finalizeEntity(@MappingTarget final VirtualEquipmentDigitalService target, final VirtualEquipmentBO source, @Context final String type) {
        // Link to Virtual Equipment Characteristic.
        String characteristicType;
        Integer characteristicValue;
        if (StringUtils.equalsIgnoreCase("compute", type)) {
            characteristicType = "vCPU";
            characteristicValue = source.getVCpu();
        } else {
            characteristicType = "Disk";
            characteristicValue = source.getDisk();
        }
        mergeCharacteristicValue(target, characteristicValue, characteristicType);
    }

    /**
     * Map after toBusinessObject.
     *
     * @param target the business entity.
     * @param source the entity.
     */
    @AfterMapping
    protected void finalizeBusinessObject(final VirtualEquipmentDigitalService source, @MappingTarget final VirtualEquipmentBO target) {
        final Integer characteristicValue = source.getVirtualEquipmentCharacteristic().getCharacteristicValue();
        if (StringUtils.equalsIgnoreCase("vCPU", source.getVirtualEquipmentCharacteristic().getType())) {
            target.setVCpu(characteristicValue);
        } else if (StringUtils.equalsIgnoreCase("Disk", source.getVirtualEquipmentCharacteristic().getType())) {
            target.setDisk(characteristicValue);
        }
    }

    private void mergeCharacteristicValue(final VirtualEquipmentDigitalService target, final Integer characteristicValue, final String characteristicType) {
        Optional.ofNullable(target.getVirtualEquipmentCharacteristic()).ifPresentOrElse(characteristic ->
                {
                    target.getVirtualEquipmentCharacteristic().setCharacteristicValue(characteristicValue);
                    target.getVirtualEquipmentCharacteristic().setType(characteristicType);
                },
                () -> target.setVirtualEquipmentCharacteristic(VirtualEquipmentCharacteristic.builder()
                        .type(characteristicType)
                        .characteristicValue(characteristicValue)
                        .build())
        );
    }

}