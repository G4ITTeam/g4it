/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice;

import com.soprasteria.g4it.backend.common.filesystem.business.FileStorage;
import com.soprasteria.g4it.backend.common.filesystem.business.FileSystem;
import com.soprasteria.g4it.backend.common.filesystem.business.local.LocalFileService;
import com.soprasteria.g4it.backend.common.filesystem.model.FileFolder;
import com.soprasteria.g4it.backend.common.filesystem.model.FileType;
import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.task.model.ITaskExecute;
import com.soprasteria.g4it.backend.common.task.model.TaskStatus;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.common.utils.LogUtils;
import com.soprasteria.g4it.backend.exception.AsyncTaskException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class AsyncLoadFilesService implements ITaskExecute {

    @Value("${local.working.folder}")
    private String localWorkingFolder;

    @Autowired
    TaskRepository taskRepository;

    @Autowired
    LoadFileService loadFileService;

    @Autowired
    LocalFileService localFileService;

    @Autowired
    private FileSystem fileSystem;

    /**
     * Execute the Task of type LOADING
     *
     * @param task the task
     */
    public void execute(final Context context, final Task task) {

        log.info("Start load input files for {}", context.log());

        long start = System.currentTimeMillis();
        final List<String> filenames = task.getFilenames();

        final List<String> details = new ArrayList<>();
        details.add(LogUtils.info("Start task"));

        task.setDetails(details);
        task.setStatus(TaskStatus.IN_PROGRESS.toString());
        taskRepository.save(task);
        final List<String> errors = new ArrayList<>();

        int fileNumber = 0;
        try {
            for (FileType fileType : List.of(FileType.DATACENTER, FileType.EQUIPEMENT_PHYSIQUE, FileType.EQUIPEMENT_VIRTUEL, FileType.APPLICATION)) {
                for (String filename : filenames) {
                    if (filename.startsWith(fileType.toString())) {
                        details.add(LogUtils.info("Manage file " + loadFileService.getOriginalFilename(fileType, filename)));
                        errors.addAll(loadFileService.manageFile(context, fileType, filename));
                        task.setProgressPercentage(fileNumber * 100 / filenames.size() + "%");
                        fileNumber++;
                    }
                }
            }

            FileStorage fileStorage = fileSystem.mount(context.getSubscriber(), context.getOrganizationId().toString());
            boolean hasRejectedFile = uploadZip(context, task, fileStorage);
            clean(fileStorage, filenames);

            details.add(LogUtils.info("Finished task successfully"));

            task.setStatus(hasRejectedFile ? TaskStatus.COMPLETED_WITH_ERRORS.toString() : TaskStatus.COMPLETED.toString());
            task.setProgressPercentage("100%");

        } catch (AsyncTaskException e) {
            log.error("Async task with id '{}' failed for '{}' with error: ", task.getId(), context.log(), e);
            task.setStatus(TaskStatus.FAILED.toString());
            details.add(LogUtils.error("Error message: " + e.getMessage()));
        } finally {
            task.setErrors(errors);
            task.setDetails(details);
        }

        taskRepository.save(task);

        loadFileService.setInventoryCounts(context.getInventoryId());

        long end = System.currentTimeMillis();
        log.info("End load input files for {}. Time taken: {}s", context.log(), (end - start) / 1000);
    }

    /**
     * Upload rejected zip file
     *
     * @param context     the context
     * @param task        the task
     * @param fileStorage the file storage
     * @return true if has any zip uploaded
     */
    private boolean uploadZip(Context context, Task task, FileStorage fileStorage) {
        try {
            final Path rejectedFolderPath = Path.of(localWorkingFolder).resolve("rejected").resolve(String.valueOf(context.getInventoryId()));
            if (Files.exists(rejectedFolderPath) && !localFileService.isEmpty(rejectedFolderPath)) {
                // create rejected zip file
                final File rejectedZipFile = localFileService.createZipFile(rejectedFolderPath, rejectedFolderPath.resolve(Constants.REJECTED_FILES_ZIP));

                // send zip to file storage
                fileStorage.upload(rejectedZipFile.getAbsolutePath(), FileFolder.OUTPUT, task.getId() + "/" + rejectedZipFile.getName());

                // clear directory
                Arrays.stream(Objects.requireNonNull(rejectedFolderPath.toFile().listFiles())).forEach(File::delete);
                return true;
            }
            return false;
        } catch (IOException e) {
            throw new AsyncTaskException("An error occurred on file upload of rejected zip file", e);
        }
    }

    /**
     * Clean filenames from local and file storage
     *
     * @param fileStorage the file storage
     * @param filenames   the filename list
     */
    private void clean(FileStorage fileStorage, List<String> filenames) {
        try {
            for (String filename : filenames) {
                Files.delete(Path.of(localWorkingFolder).resolve("input/inventory").resolve(filename));
            }

            for (String filename : filenames) {
                fileStorage.delete(FileFolder.INPUT, filename);
            }
        } catch (IOException e) {
            throw new AsyncTaskException("An error occurred on cleaning files in local or remote storage", e);
        }
    }

}
