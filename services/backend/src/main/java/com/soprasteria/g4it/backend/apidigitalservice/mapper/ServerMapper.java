/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apidigitalservice.mapper;

import com.soprasteria.g4it.backend.apidigitalservice.business.DigitalServiceReferentialService;
import com.soprasteria.g4it.backend.apidigitalservice.model.ServerBO;
import com.soprasteria.g4it.backend.apidigitalservice.model.VirtualEquipmentBO;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.Server;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.ServerCharacteristic;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DatacenterDigitalServiceRepository;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Server Mapper.
 */
@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true),
        collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
        uses = {DatacenterDigitalServiceMapper.class, VirtualEquipmentMapper.class,
                DigitalServiceReferentialMapper.class})
public abstract class ServerMapper {

    /**
     * The repository to access datacenter digital service data.
     */
    @Autowired
    private DatacenterDigitalServiceRepository datacenterDigitalServiceRepository;

    /**
     * Virtual Equipment Mapper.
     */
    @Autowired
    private VirtualEquipmentMapper virtualEquipmentMapper;

    /**
     * Map the business object in entity object.
     *
     * @param source the business object.
     * @return the server entity.
     */
    @Mapping(target = "datacenterDigitalService", source = "datacenter")
    @Mapping(target = "serverHost", expression = "java(digitalServiceReferentialService.getServerHost(source.getHost().getCode()))")
    @Mapping(target = "annualElectricityConsumption", source = "annualElectricConsumption")
    public abstract Server toEntity(final ServerBO source, @Context final DigitalServiceReferentialService digitalServiceReferentialService);

    /**
     * Map the business object in entity object list.
     *
     * @param source the business object.
     * @return the server entity list.
     */
    public abstract List<Server> toEntity(final List<ServerBO> source, @Context final DigitalServiceReferentialService digitalServiceReferentialService);

    /**
     * Merge entity.
     *
     * @param target                           server entity to update.
     * @param source                           server containing new data.
     * @param digitalServiceReferentialService service to retrieve referential data.
     */
    @Mapping(target = "datacenterDigitalService", source = "datacenter")
    @Mapping(target = "serverHost", expression = "java(digitalServiceReferentialService.getServerHost(source.getHost().getCode()))")
    @Mapping(target = "annualElectricityConsumption", source = "annualElectricConsumption")
    public abstract void merge(@MappingTarget final Server target, final ServerBO source, @Context final DigitalServiceReferentialService digitalServiceReferentialService);

    /**
     * Map to Business Object.
     *
     * @param source the source.
     * @return the ServerBO.
     */
    @Mapping(target = "datacenter", source = "datacenterDigitalService")
    @Mapping(target = "vm", source = "virtualEquipmentDigitalServices")
    @Mapping(target = "annualElectricConsumption", source = "annualElectricityConsumption")
    @Mapping(target = "host", source = "serverHost")
    public abstract ServerBO toBusinessObject(final Server source);

    /**
     * Map to Business Object list.
     *
     * @param source the source list.
     * @return the business object list.
     */
    public abstract List<ServerBO> toBusinessObjects(final List<Server> source);

    /**
     * Link entity datacenter to server.
     *
     * @param source                           the business object containing datacenter uid.
     * @param target                           the entity to link.
     * @param digitalServiceReferentialService here just for method signature
     */
    @AfterMapping
    protected void finalizeEntity(@MappingTarget final Server target, final ServerBO source, @Context final DigitalServiceReferentialService digitalServiceReferentialService) {
        // Link to Datacenter.
        if (Strings.isNotEmpty(source.getDatacenter().getUid())) {
            // Link server to datacenter if exists.
            target.setDatacenterDigitalService(datacenterDigitalServiceRepository.findByUid(source.getDatacenter().getUid()).orElseThrow(() -> new G4itRestException("404", source.getDatacenter().getName() + " is not found.")));
        } else {
            // Link server to a new datacenter.
            target.getDatacenterDigitalService().setServer(List.of(target));
        }

        // Link to Server Characteristic.
        String characteristicType;
        Integer characteristicValue;
        if (StringUtils.equalsIgnoreCase("compute", source.getType())) {
            characteristicType = "vCPU";
            characteristicValue = source.getTotalVCpu();
        } else {
            characteristicType = "Disk";
            characteristicValue = source.getTotalDisk();
        }
        mergeCharacteristicValue(target, characteristicValue, characteristicType);

        // Detect removed Vm.
        Optional.ofNullable(target.getVirtualEquipmentDigitalServices()).orElse(new ArrayList<>())
                .removeIf(vm -> !Optional.ofNullable(source.getVm()).orElse(new ArrayList<>())
                        .stream()
                        .map(VirtualEquipmentBO::getUid)
                        .toList()
                        .contains(vm.getUid()));
        Optional.ofNullable(source.getVm()).orElse(new ArrayList<>()).forEach(vm -> mergeVirtualEquipment(target, vm));
    }

    /**
     * Map after toBusinessObject.
     *
     * @param source the entity.
     * @param target the business entity.
     */
    @AfterMapping
    protected void mapBusinessObject(final Server source, @MappingTarget final ServerBO target) {
        final Integer characteristicValue = source.getServerCharacteristic().getCharacteristicValue();
        if (StringUtils.equalsIgnoreCase("compute", target.getType())) {
            target.setTotalVCpu(characteristicValue);
        } else if (StringUtils.equalsIgnoreCase("storage", target.getType())) {
            target.setTotalDisk(characteristicValue);
        }
    }

    private void mergeCharacteristicValue(final Server target, final Integer characteristicValue, final String characteristicType) {
        Optional.ofNullable(target.getServerCharacteristic()).ifPresentOrElse(characteristic ->
                {
                    target.getServerCharacteristic().setCharacteristicValue(characteristicValue);
                    target.getServerCharacteristic().setType(characteristicType);
                },
                () -> target.setServerCharacteristic(ServerCharacteristic.builder()
                        .type(characteristicType)
                        .characteristicValue(characteristicValue)
                        .build())
        );
    }

    private void mergeVirtualEquipment(final Server target, final VirtualEquipmentBO virtualEquipment) {
        if (StringUtils.isEmpty(virtualEquipment.getUid())) {
            target.addVirtualEquipment(virtualEquipmentMapper.toEntity(virtualEquipment, target.getType()));
        } else {
            virtualEquipmentMapper.merge(
                    Optional.ofNullable(target.getVirtualEquipmentDigitalServices()).orElse(new ArrayList<>())
                            .stream()
                            .filter(vm -> vm.getUid().equals(virtualEquipment.getUid()))
                            .findFirst().orElseThrow(),
                    virtualEquipment, target.getType());
        }
    }

}
