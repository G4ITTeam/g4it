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
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

    @Autowired
    TaskRepository taskRepository;

    public String evaluateAllDigitalServices() {
        List<DigitalService> digitalServices = digitalServiceRepository.findAll();
        int count = 1;
        final LocalDateTime now = LocalDateTime.now();
        boolean runEvaluation = true;
        int skipEvaluationCount = 0;
        log.info("Digital-service count before evaluation- {}", digitalServices.size());
        for (DigitalService digitalService : digitalServices) {
            //evaluating
            String digitalServiceUid = digitalService.getUid();
            Organization organization = organizationRepository.findById(digitalService.getOrganization().getId()).orElseThrow();
            Long organizationId = organization.getId();
            String subscriber = organization.getSubscriber().getName();

            List<InPhysicalEquipment> physicalEquipments = physicalEquipmentRepository.findByDigitalServiceUid(digitalServiceUid);
            if (!physicalEquipments.isEmpty()) {
                Optional<Task> tasks = taskRepository.findByDigitalServiceUid(digitalService.getUid());
                if (tasks.isPresent()) {
                    Task task = tasks.get();
                    int day = task.getLastUpdateDate().getDayOfMonth();
                    int month = task.getLastUpdateDate().getMonthValue();
                    int year = task.getLastUpdateDate().getYear();
                    if ((now.getDayOfMonth() == day || now.getDayOfMonth() - 1 == day) && now.getMonthValue() == month && now.getYear() == year && "COMPLETED".equals(task.getStatus())) {
                        runEvaluation = false;
                        skipEvaluationCount++;
                    }
                }
                List<InPhysicalEquipment> networkEquipments = physicalEquipments.stream().filter(physicalEquipment -> "Network".equals(physicalEquipment.getType())).toList();
                if (!networkEquipments.isEmpty() && runEvaluation) {
                    log.info("Digital-service re-evaluation and count- {}: {}", digitalServiceUid, count);
                    evaluatingService.evaluatingDigitalService(subscriber, organizationId, digitalServiceUid);
                    count++;
                }
            }
        }
        log.info("re-evaluation total count- {}", count);
        log.info("skip-evaluation total count- {}", skipEvaluationCount);
        return "success";
    }

}