/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apidigitalservice.mapper;

import com.soprasteria.g4it.backend.apidigitalservice.business.DigitalServiceReferentialService;
import com.soprasteria.g4it.backend.apidigitalservice.model.TerminalBO;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.Terminal;
import org.mapstruct.*;

import java.util.List;

/**
 * Terminal Mapper.
 */
@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true), uses = {DigitalServiceReferentialMapper.class})
public interface TerminalMapper {

    /**
     * Map to business object.
     *
     * @param source terminal entity.
     * @return terminal business object.
     */
    @Mapping(source = "deviceType", target = "type")
    TerminalBO toBusinessObject(final Terminal source);

    /**
     * Map to business objects.
     *
     * @param source list of terminal entity.
     * @return list of terminal business object.
     */
    List<TerminalBO> toBusinessObject(final List<Terminal> source);

    /**
     * Map to entity.
     *
     * @param source                           terminal business object.
     * @param digitalServiceReferentialService service to retrieve referential data.
     * @return terminal entity.
     */
    @Mapping(target = "deviceType", ignore = true)
    Terminal toEntity(final TerminalBO source, @Context final DigitalServiceReferentialService digitalServiceReferentialService);

    /**
     * Merge entity.
     *
     * @param target                           terminal entity to update.
     * @param source                           terminal containing new data.
     * @param digitalServiceReferentialService service to retrieve referential data.
     */
    @Mapping(target = "lastUpdateDate", expression = "java(java.time.LocalDateTime.now())")
    void merge(@MappingTarget final Terminal target, final TerminalBO source, @Context final DigitalServiceReferentialService digitalServiceReferentialService);

    /**
     * Map device type.
     *
     * @param target                           the terminal entity.
     * @param source                           the terminal business object.
     * @param digitalServiceReferentialService service to retrieve referential data.
     */
    @AfterMapping
    default void mapType(@MappingTarget final Terminal target, final TerminalBO source, @Context final DigitalServiceReferentialService digitalServiceReferentialService) {
        target.setDeviceType(digitalServiceReferentialService.getTerminalDeviceType(source.getType().getCode()));
    }

}
