/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.business;

import com.soprasteria.g4it.backend.apifiles.business.FileSystemService;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.apiinventory.repository.InventoryRepository;
import com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.AsyncLoadFilesService;
import com.soprasteria.g4it.backend.apiuser.business.OrganizationService;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import com.soprasteria.g4it.backend.common.filesystem.model.FileType;
import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.task.model.BackgroundTask;
import com.soprasteria.g4it.backend.common.task.model.TaskStatus;
import com.soprasteria.g4it.backend.common.task.model.TaskType;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import com.soprasteria.g4it.backend.common.utils.StringUtils;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

@Service
@Slf4j
public class LoadInputFilesService {

    @Autowired
    OrganizationService organizationService;

    @Autowired
    TaskRepository taskRepository;

    @Autowired
    InventoryRepository inventoryRepository;
    @Autowired
    @Qualifier("taskExecutorSingleThreaded")
    TaskExecutor taskExecutor;
    /**
     * Async Service where is executed the file loading
     */
    @Autowired
    AsyncLoadFilesService asyncLoadFilesService;
    @Autowired
    private FileSystemService fileSystemService;

    /**
     * Load input files for an inventory
     *
     * @param subscriber         the subscriber
     * @param organizationId     the organization id
     * @param inventoryId        the inventory id
     * @param datacenters        the datacenter files
     * @param physicalEquipments the physical equipment files
     * @param virtualEquipments  the virtual equipment files
     * @param applications       the application files
     * @return the Task created
     */
    public Task loadFiles(final String subscriber,
                          final Long organizationId,
                          final Long inventoryId,
                          final List<MultipartFile> datacenters,
                          final List<MultipartFile> physicalEquipments,
                          final List<MultipartFile> virtualEquipments,
                          final List<MultipartFile> applications) {

        final Map<FileType, List<MultipartFile>> allFiles = new EnumMap<>(FileType.class);

        if (datacenters != null) allFiles.put(FileType.DATACENTER, datacenters);
        if (physicalEquipments != null) allFiles.put(FileType.EQUIPEMENT_PHYSIQUE, physicalEquipments);
        if (virtualEquipments != null) allFiles.put(FileType.EQUIPEMENT_VIRTUEL, virtualEquipments);
        if (applications != null) allFiles.put(FileType.APPLICATION, applications);

        if (allFiles.isEmpty()) return new Task();

        Inventory inventory = inventoryRepository.findById(inventoryId).orElseThrow();

        List<Task> tasks = taskRepository.findByInventoryAndStatusAndType(inventory, TaskStatus.IN_PROGRESS.toString(), TaskType.LOADING.toString());
        if (!tasks.isEmpty()) {
            throw new G4itRestException("500", "task.already.running");
        }

        Context context = Context.builder()
                .subscriber(subscriber)
                .organizationId(organizationId)
                .organizationName(organizationService.getOrganizationById(organizationId).getName())
                .inventoryId(inventoryId)
                .datetime(LocalDateTime.now())
                .hasVirtualEquipments(inventory.getVirtualEquipmentCount() > 0)
                .hasApplications(inventory.getApplicationCount() > 0)
                .build();


        // store files into file storage
        List<String> filenames = Stream.of(FileType.DATACENTER, FileType.EQUIPEMENT_PHYSIQUE, FileType.EQUIPEMENT_VIRTUEL, FileType.APPLICATION)
                .map(fileType -> {
                    List<MultipartFile> files = allFiles.get(fileType);
                    List<String> typeFileNames = newFilenames(files, fileType);
                    fileSystemService.manageFilesAndRename(context.getSubscriber(), context.getOrganizationId(), files, typeFileNames);
                    return typeFileNames;
                })
                .flatMap(Collection::stream)
                .toList();

        // create task with type LOADING
        Task task = Task.builder()
                .creationDate(context.getDatetime())
                .details(new ArrayList<>())
                .lastUpdateDate(context.getDatetime())
                .progressPercentage("0%")
                .status(TaskStatus.TO_START.toString())
                .type(TaskType.LOADING.toString())
                .inventory(Inventory.builder().id(inventoryId).build())
                .filenames(filenames)
                .createdBy(inventory.getCreatedBy())
                .build();

        taskRepository.save(task);

        // run loading async task
        taskExecutor.execute(new BackgroundTask(context, task, asyncLoadFilesService));

        return task;
    }

    /**
     * Get task with type LOADING and IN_PROGRESS and lastUpdateDate > 1 min from now
     * Change the status to TO_START and execute the task in background
     */
    @Transactional
    public void restartLoadingFiles() {
        List<Task> inProgressLoadingTasks = taskRepository.findByStatusAndType(TaskStatus.IN_PROGRESS.toString(), TaskType.LOADING.toString());

        if (inProgressLoadingTasks.isEmpty()) return;

        final LocalDateTime now = LocalDateTime.now();

        // check tasks to restart
        inProgressLoadingTasks.stream()
                .filter(task -> task.getLastUpdateDate().plusMinutes(15).isBefore(now))
                .forEach(task -> {
                    task.setStatus(TaskStatus.TO_START.toString());
                    task.setLastUpdateDate(now);
                    task.setDetails(new ArrayList<>());
                    task.setProgressPercentage("0%");
                    taskRepository.save(task);

                    final Inventory inventory = task.getInventory();
                    final Organization organization = inventory.getOrganization();
                    final Context context = Context.builder()
                            .subscriber(organization.getSubscriber().getName())
                            .organizationId(organization.getId())
                            .organizationName(organization.getName())
                            .inventoryId(task.getInventory().getId())
                            .locale(Locale.getDefault())
                            .datetime(now)
                            .hasVirtualEquipments(inventory.getVirtualEquipmentCount() > 0)
                            .hasApplications(inventory.getApplicationCount() > 0)
                            .build();

                    log.warn("Restart task {} with taskId={}", TaskType.LOADING, task.getId());
                    taskExecutor.execute(new BackgroundTask(
                            context,
                            task,
                            asyncLoadFilesService)
                    );
                });
    }


    /**
     * Assign new unique file names to input files
     * target names: ${type}_${UUID}.csv where type is the FileType enum
     *
     * @param files the input files
     * @param type  the type
     * @return the new list of file names
     */
    private List<String> newFilenames(List<MultipartFile> files, final FileType type) {
        if (files == null) return new ArrayList<>();
        return files.stream()
                .map(file -> {
                    String originalFilename = file.getOriginalFilename();
                    // ensures the original filename can be properly matched with regex later
                    originalFilename = originalFilename == null ? "" : originalFilename.replace("_", "-");
                    String extension = StringUtils.getFilenameExtension(originalFilename);
                    return String.format("%s_%s_%s.%s", type.toString(), originalFilename, UUID.randomUUID(), extension);
                })
                .toList();
    }
}
