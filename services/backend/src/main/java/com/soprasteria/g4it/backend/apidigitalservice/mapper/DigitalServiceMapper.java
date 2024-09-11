/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apidigitalservice.mapper;

import com.soprasteria.g4it.backend.apidigitalservice.business.DigitalServiceReferentialService;
import com.soprasteria.g4it.backend.apidigitalservice.model.DigitalServiceBO;
import com.soprasteria.g4it.backend.apidigitalservice.model.NetworkBO;
import com.soprasteria.g4it.backend.apidigitalservice.model.ServerBO;
import com.soprasteria.g4it.backend.apidigitalservice.model.TerminalBO;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalService;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.Server;
import com.soprasteria.g4it.backend.apiuser.modeldb.User;
import com.soprasteria.g4it.backend.common.dbmodel.Note;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * DigitalService Mapper.
 */
@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true),
        collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
        uses = {TerminalMapper.class, NetworkMapper.class, ServerMapper.class})
public abstract class DigitalServiceMapper {

    @Autowired
    private TerminalMapper terminalMapper;

    @Autowired
    private NetworkMapper networkMapper;

    @Autowired
    private ServerMapper serverMapper;

    @Autowired
    private NoteMapper noteMapper;

    /**
     * Map to Business Object.
     *
     * @param entity the source.
     * @return the DigitalServiceBO.
     */
    @Mapping(target = "terminals", ignore = true)
    @Mapping(target = "networks", ignore = true)
    @Mapping(target = "servers", ignore = true)
    @Mapping(target = "userId", source = "user.id")
    public abstract DigitalServiceBO toBusinessObject(final DigitalService entity);

    /**
     * Map to Business Object list.
     *
     * @param source the source list.
     * @return the business object list.
     */
    @Mapping(target = "userId", source = "user.id")
    public abstract List<DigitalServiceBO> toBusinessObject(final List<DigitalService> source);

    /**
     * Map to Business Object.
     *
     * @param entity the source.
     * @return the DigitalServiceBO.
     */
    @Named("fullMapping")
    @Mapping(target = "userId", source = "user.id")
    public abstract DigitalServiceBO toFullBusinessObject(final DigitalService entity);

    /**
     * Map to Business Object list.
     *
     * @param source the source list.
     * @return the business object list.
     */
    @IterableMapping(qualifiedByName = "fullMapping")
    public abstract List<DigitalServiceBO> toFullBusinessObject(final List<DigitalService> source);

    /**
     * Map the business object in entity object.
     *
     * @param source                           the business object.
     * @param digitalServiceReferentialService service to retrieve referential data.
     * @return the digital service entity.
     */
    public abstract DigitalService toEntity(final DigitalServiceBO source, @Context final DigitalServiceReferentialService digitalServiceReferentialService);

    /**
     * Merge two entities to update.
     *
     * @param target                           the digital service to update.
     * @param source                           the digital service from frontend.
     * @param digitalServiceReferentialService the service to retrieve referential data.
     */
    public void mergeEntity(@MappingTarget final DigitalService target, final DigitalServiceBO source,
                            @Context final DigitalServiceReferentialService digitalServiceReferentialService, @Context final User user) {
        if (source == null) {
            return;
        }

        target.setName(source.getName());
        target.setLastUpdateDate(LocalDateTime.now());

        // Merge terminals.
        Optional.ofNullable(target.getTerminals()).orElse(new ArrayList<>())
                .removeIf(terminal -> !Optional.ofNullable(source.getTerminals()).orElse(new ArrayList<>())
                        .stream()
                        .map(TerminalBO::getUid)
                        .toList()
                        .contains(terminal.getUid()));
        Optional.ofNullable(source.getTerminals()).orElse(new ArrayList<>()).forEach(terminal -> mergeTerminal(target, terminal, digitalServiceReferentialService));

        // Merge networks.
        Optional.ofNullable(target.getNetworks()).orElse(new ArrayList<>())
                .removeIf(network -> !Optional.ofNullable(source.getNetworks()).orElse(new ArrayList<>())
                        .stream()
                        .map(NetworkBO::getUid)
                        .toList()
                        .contains(network.getUid()));
        Optional.ofNullable(source.getNetworks()).orElse(new ArrayList<>()).forEach(network -> mergeNetwork(target, network, digitalServiceReferentialService));

        // Merge servers.
        Optional.ofNullable(target.getServers()).orElse(new ArrayList<>())
                .removeIf(server -> !Optional.ofNullable(source.getServers()).orElse(new ArrayList<>())
                        .stream()
                        .map(ServerBO::getUid)
                        .toList()
                        .contains(server.getUid()));
        Optional.ofNullable(source.getServers()).orElse(new ArrayList<>()).forEach(server -> mergeServer(target, server, digitalServiceReferentialService));

        // Merge note
        Note note = noteMapper.toEntity(source.getNote());
        if (note != null) {
            if (target.getNote() == null) {
                note.setCreatedBy(user);
            }
            note.setLastUpdatedBy(user);
        }
        target.setNote(note);
    }

    /**
     * Merge terminal.
     *
     * @param target                           the entity to update.
     * @param source                           the business object containing updated data.
     * @param digitalServiceReferentialService to retrieve referential data.
     */
    private void mergeTerminal(final DigitalService target, final TerminalBO source,
                               final DigitalServiceReferentialService digitalServiceReferentialService) {
        // Fix update cascade list.
        if (StringUtils.isEmpty(source.getUid())) {
            target.addTerminal(terminalMapper.toEntity(source, digitalServiceReferentialService));
        } else {
            terminalMapper.merge(target.getTerminals().stream().filter(ter -> ter.getUid().equals(source.getUid())).findFirst().orElseThrow(), source, digitalServiceReferentialService);
        }
    }

    /**
     * Merge network.
     *
     * @param target                           the entity to update.
     * @param source                           the business object containing updated data.
     * @param digitalServiceReferentialService to retrieve referential data.
     */
    private void mergeNetwork(final DigitalService target, final NetworkBO source,
                              final DigitalServiceReferentialService digitalServiceReferentialService) {
        if (StringUtils.isEmpty(source.getUid())) {
            target.addNetwork(networkMapper.toEntity(source, digitalServiceReferentialService));
        } else {
            networkMapper.merge(target.getNetworks().stream().filter(ter -> ter.getUid().equals(source.getUid())).findFirst().orElseThrow(), source, digitalServiceReferentialService);
        }
    }

    /**
     * Merge servers.
     *
     * @param target                           the entity to update.
     * @param source                           the business object containing updated data.
     * @param digitalServiceReferentialService to retrieve referential data.
     */
    private void mergeServer(final DigitalService target, final ServerBO source,
                             final DigitalServiceReferentialService digitalServiceReferentialService) {
        if (StringUtils.isEmpty(source.getUid())) {
            target.addServer(serverMapper.toEntity(source, digitalServiceReferentialService));
        } else {
            serverMapper.merge(target.getServers().stream().filter(ter -> ter.getUid().equals(source.getUid())).findFirst().orElseThrow(), source, digitalServiceReferentialService);
        }
        target.getServers().stream().map(Server::getDatacenterDigitalService).filter(Objects::nonNull).forEach(target::addDatacenter);
    }

}
