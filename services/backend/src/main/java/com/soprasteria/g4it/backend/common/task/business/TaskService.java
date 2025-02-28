/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.common.task.business;

import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalService;
import com.soprasteria.g4it.backend.common.task.mapper.TaskMapper;
import com.soprasteria.g4it.backend.common.task.model.TaskStatus;
import com.soprasteria.g4it.backend.common.task.model.TaskType;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import com.soprasteria.g4it.backend.server.gen.api.dto.TaskRest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class TaskService {

    @Autowired
    TaskRepository taskRepository;

    @Autowired
    TaskMapper taskMapper;

    /**
     * Get task by taskId
     *
     * @param taskId the task id
     * @return the task
     */
    public TaskRest getTask(final long taskId) {

        final Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new G4itRestException("404", String.format("task %d not found", taskId)));

        return taskMapper.map(task);
    }

    /**
     * Create digital service task
     *
     * @param digitalService the digital service
     * @param criteria       the list of criterion
     * @return the task
     */
    public Task createDigitalServiceTask(DigitalService digitalService, List<String> criteria) {
        final LocalDateTime now = LocalDateTime.now();
        Task task = taskRepository.findByDigitalServiceUid(digitalService.getUid())
                .orElseGet(() -> Task.builder()
                        .digitalServiceUid(digitalService.getUid())
                        .type(TaskType.EVALUATING_DIGITAL_SERVICE.toString())
                        .build());

        task.setProgressPercentage("0%");
        task.setStatus(TaskStatus.IN_PROGRESS.toString());
        task.setCreationDate(now);
        task.setLastUpdateDate(now);
        task.setCriteria(criteria);

        return taskRepository.save(task);
    }

    /**
     * Get the task from digital service uid
     *
     * @param digitalServiceUid the digital service uid
     * @return the task
     */
    public Optional<Task> getTask(String digitalServiceUid) {
        return taskRepository.findByDigitalServiceUid(digitalServiceUid);
    }

    /**
     * Save task in database
     *
     * @param task the task
     */
    public void saveTask(Task task) {
        taskRepository.save(task);
    }

}
