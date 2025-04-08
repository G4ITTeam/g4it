/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiadministratoractions.business;

import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalService;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceRepository;
import com.soprasteria.g4it.backend.apievaluating.business.EvaluatingService;
import com.soprasteria.g4it.backend.apiinout.modeldb.InPhysicalEquipment;
import com.soprasteria.g4it.backend.apiinout.repository.InPhysicalEquipmentRepository;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import com.soprasteria.g4it.backend.apiuser.repository.OrganizationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class AdministratorActionsService {

    @Autowired
    EvaluatingService evaluatingService;

    @Autowired
    DigitalServiceRepository digitalServiceRepository;

    @Autowired
    OrganizationRepository organizationRepository;

    @Autowired
    InPhysicalEquipmentRepository physicalEquipmentRepository;

    public String evaluateAllDigitalServices() {
        List<DigitalService> digitalServices = digitalServiceRepository.findAll();
        int count = 0;
        for (DigitalService digitalService : digitalServices) {
            //evaluating
            String digitalServiceUid = digitalService.getUid();
            Organization organization = organizationRepository.findById(digitalService.getOrganization().getId()).orElseThrow();
            Long organizationId = organization.getId();
            String subscriber = organization.getSubscriber().getName();
            List<InPhysicalEquipment> physicalEquipments = physicalEquipmentRepository.findByDigitalServiceUid(digitalServiceUid);
            if (!physicalEquipments.isEmpty()) {
                List<InPhysicalEquipment> networkEquipments = physicalEquipments.stream().filter(physicalEquipment -> "Network".equals(physicalEquipment.getType())).toList();
                if (!networkEquipments.isEmpty()) {
                    log.info("Digital-service re-evaluation - {}", digitalServiceUid);
                    evaluatingService.evaluatingDigitalService(subscriber, organizationId, digitalServiceUid, true);
                    count++;
                }
            }
        }
        log.info("Digital-service re-evaluation count- {}", count);
        return "success";
    }

}