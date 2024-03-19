/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apidigitalservice.mapper;

import com.soprasteria.g4it.backend.apidigitalservice.business.DigitalServiceReferentialService;
import com.soprasteria.g4it.backend.apidigitalservice.model.NetworkBO;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.Network;
import org.mapstruct.*;

import java.util.List;

/**
 * Network Mapper.
 */
@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true), uses = {DigitalServiceReferentialMapper.class})
public interface NetworkMapper {

    /**
     * Map to business object.
     *
     * @param source network entity.
     * @return network business object.
     */
    @Mapping(source = "networkType", target = "type")
    NetworkBO toBusinessObject(final Network source);

    /**
     * Map to business objects.
     *
     * @param source list of network entity.
     * @return list of network business object.
     */
    List<NetworkBO> toBusinessObject(final List<Network> source);

    /**
     * Map to entity.
     *
     * @param source                           network business object.
     * @param digitalServiceReferentialService service to retrieve referential data.
     * @return network entity.
     */
    @Mapping(target = "networkType", ignore = true)
    Network toEntity(final NetworkBO source, @Context final DigitalServiceReferentialService digitalServiceReferentialService);

    /**
     * Merge entity.
     *
     * @param target                           network entity to update.
     * @param source                           network containing new data.
     * @param digitalServiceReferentialService service to retrieve referential data.
     */
    @Mapping(target = "lastUpdateDate", expression = "java(java.time.LocalDateTime.now())")
    void merge(@MappingTarget final Network target, final NetworkBO source, @Context final DigitalServiceReferentialService digitalServiceReferentialService);

    /**
     * Map device type.
     *
     * @param target                           the network entity.
     * @param source                           the network business object.
     * @param digitalServiceReferentialService service to retrieve referential data.
     */
    @AfterMapping
    default void mapType(@MappingTarget final Network target, final NetworkBO source, @Context final DigitalServiceReferentialService digitalServiceReferentialService) {
        target.setNetworkType(digitalServiceReferentialService.getNetworkType(source.getType().getCode()));
    }

}
