/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.loadobject;

import com.soprasteria.g4it.backend.apiinout.mapper.InApplicationMapper;
import com.soprasteria.g4it.backend.apiinout.modeldb.InApplication;
import com.soprasteria.g4it.backend.apiinout.repository.InApplicationRepository;
import com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.checkobject.CheckApplicationService;
import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.model.LineError;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.server.gen.api.dto.InApplicationRest;
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
public class LoadApplicationService {

    @Autowired
    CheckApplicationService checkApplicationService;

    @Autowired
    InApplicationMapper inApplicationMapper;

    @Autowired
    InApplicationRepository inApplicationRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public List<LineError> execute(final Context context, final int pageNumber, List<InApplicationRest> applications) {
        if (applications.isEmpty()) return List.of();

        log.info("Load applications for {}, size = {}", context.log(), applications.size());

        List<LineError> errors = new ArrayList<>();
        List<InApplication> applicationsToSave = new ArrayList<>();

        for (int i = 0; i < applications.size(); i++) {
            int line = Constants.BATCH_SIZE * pageNumber + i + 2;

            final List<LineError> checkErrors = checkApplicationService.checkRules(context, applications.get(i), line);
            if (checkErrors.isEmpty()) {
                applicationsToSave.add(inApplicationMapper.toEntity(applications.get(i)));
            } else {
                errors.addAll(checkErrors);
            }
        }
        // Delete existing applications
        final Set<String> names = applications.stream().map(InApplicationRest::getName).collect(Collectors.toSet());
        inApplicationRepository.deleteByInventoryIdAndNameIn(context.getInventoryId(), names);

        // Load data into database
        inApplicationRepository.saveAll(applicationsToSave);
        entityManager.flush();
        entityManager.clear();

        applicationsToSave.clear();
        return errors;
    }

    public Long getApplicationCount(Long inventoryId) {
        return inApplicationRepository.countDistinctNameByInventoryId(inventoryId);
    }
}
