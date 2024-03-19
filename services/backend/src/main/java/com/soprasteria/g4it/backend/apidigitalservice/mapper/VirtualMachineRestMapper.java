/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apidigitalservice.mapper;

import com.soprasteria.g4it.backend.apidigitalservice.model.VirtualEquipmentBO;
import com.soprasteria.g4it.backend.server.gen.api.dto.ServerVirtualMachineRest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * DigitalServiceRest Mapper.
 */
@Mapper(componentModel = "spring")
public interface VirtualMachineRestMapper {

    /**
     * Map to Data Transfer Object.
     *
     * @param source the source.
     * @return the ServerVirtualMachineRest.
     */
    @Mapping(target = "vCpu", source = "VCpu")
    ServerVirtualMachineRest toDto(final VirtualEquipmentBO source);

    /**
     * Map to Data Transfer Objects.
     *
     * @param source the source.
     * @return the ServerVirtualMachineRest List.
     */
    List<ServerVirtualMachineRest> toDto(final List<VirtualEquipmentBO> source);

    /**
     * Map to Business Object.
     *
     * @param source the source.
     * @return the VirtualEquipmentBO.
     */
    VirtualEquipmentBO toBusinessObject(final ServerVirtualMachineRest source);

    /**
     * Map to Business Objects.
     *
     * @param source the source.
     * @return the VirtualEquipmentBO List.
     */
    List<VirtualEquipmentBO> toBusinessObjects(final List<ServerVirtualMachineRest> source);
}
