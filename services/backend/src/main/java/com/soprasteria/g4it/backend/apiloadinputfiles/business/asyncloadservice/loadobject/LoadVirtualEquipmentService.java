/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.loadobject;

import com.soprasteria.g4it.backend.apiinout.mapper.InVirtualEquipmentMapper;
import com.soprasteria.g4it.backend.apiinout.modeldb.InVirtualEquipment;
import com.soprasteria.g4it.backend.apiinout.repository.InApplicationRepository;
import com.soprasteria.g4it.backend.apiinout.repository.InVirtualEquipmentRepository;
import com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.checkobject.CheckVirtualEquipmentService;
import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.model.LineError;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.server.gen.api.dto.InVirtualEquipmentRest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class LoadVirtualEquipmentService {

    @Autowired
    CheckVirtualEquipmentService checkVirtualEquipmentService;

    @Autowired
    InVirtualEquipmentMapper inVirtualEquipmentMapper;

    @Autowired
    InVirtualEquipmentRepository inVirtualEquipmentRepository;

    @Autowired
    InApplicationRepository inApplicationRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public List<LineError> execute(final Context context, final int pageNumber, List<InVirtualEquipmentRest> virtualEquipments) {
        if (virtualEquipments.isEmpty()) return List.of();

        log.info("Load virtual equipments for {}, size = {}", context.log(), virtualEquipments.size());

        List<LineError> errors = new ArrayList<>();
        List<InVirtualEquipment> virtualEquipmentsToSave = new ArrayList<>();

        for (int i = 0; i < virtualEquipments.size(); i++) {
            int line = Constants.BATCH_SIZE * pageNumber + i + 2;

            final List<LineError> checkErrors = checkVirtualEquipmentService.checkRules(context, virtualEquipments.get(i), line);
            if (checkErrors.isEmpty()) {
                virtualEquipmentsToSave.add(inVirtualEquipmentMapper.toEntity(virtualEquipments.get(i)));
            } else {
                errors.addAll(checkErrors);
            }
        }

        // Delete existing virtual equipments and its sub objects
        final Set<String> names = virtualEquipments.stream().map(InVirtualEquipmentRest::getName).collect(Collectors.toSet());
        inVirtualEquipmentRepository.deleteByInventoryIdAndNameIn(context.getInventoryId(), names);
        if (context.isHasApplications()) {
            inApplicationRepository.deleteByInventoryIdAndVirtualEquipmentNameIn(context.getInventoryId(), names);
        }

        // Load data into database
        inVirtualEquipmentRepository.saveAll(virtualEquipmentsToSave);
        entityManager.flush();
        entityManager.clear();

        virtualEquipmentsToSave.clear();
        return errors;
    }

    public Long getVirtualEquipmentCount(Long inventoryId) {
        return inVirtualEquipmentRepository.sumQuantityByInventoryId(inventoryId);
    }
}
