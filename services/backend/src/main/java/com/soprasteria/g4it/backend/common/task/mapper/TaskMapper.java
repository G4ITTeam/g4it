/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.common.task.mapper;

import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.server.gen.api.dto.TaskIdRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.TaskRest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Application mapper.
 */
@Mapper(componentModel = "spring")
public interface TaskMapper {

    TaskRest map(final Task task);

    @Mapping(target = "taskId", source = "id")
    TaskIdRest mapTaskId(final Task task);
}
