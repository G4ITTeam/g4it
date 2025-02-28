/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apibatchloading.listener;

import com.soprasteria.g4it.backend.apibatchloading.model.CustomExitStatus;
import com.soprasteria.g4it.backend.apibatchloading.model.InventoryLoadingSession;
import com.soprasteria.g4it.backend.apiinout.repository.InApplicationRepository;
import com.soprasteria.g4it.backend.apiinout.repository.InVirtualEquipmentRepository;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.apiinventory.modeldb.InventoryIntegrationReport;
import com.soprasteria.g4it.backend.apiinventory.repository.ApplicationRepository;
import com.soprasteria.g4it.backend.apiinventory.repository.InventoryRepository;
import com.soprasteria.g4it.backend.apiinventory.repository.PhysicalEquipmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.soprasteria.g4it.backend.apibatchloading.steps.application.config.ApplicationVirtualEquipmentConsistencyCheckStepConfiguration.APPLICATION_VIRTUAL_EQUIPMENT_CONSISTENCY_CHECK_STEP_NAME;
import static com.soprasteria.g4it.backend.apibatchloading.steps.application.config.FlagApplicationDataStepConfiguration.FLAG_APPLICATION_STEP_NAME;
import static com.soprasteria.g4it.backend.apibatchloading.steps.datacenter.config.FlagDataCenterDataStepConfiguration.FLAG_DATACENTER_STEP_NAME;
import static com.soprasteria.g4it.backend.apibatchloading.steps.datacenter.config.ValidateDataCenterHeaderStepConfiguration.VALIDATE_DATACENTER_HEADER_STEP_NAME;
import static com.soprasteria.g4it.backend.apibatchloading.steps.physicalequipment.config.FlagPhysicalEquipmentDataStepConfiguration.FLAG_PHYSICAL_EQUIPMENT_STEP_NAME;
import static com.soprasteria.g4it.backend.apibatchloading.steps.physicalequipment.config.PhysicalEquipmentDataCenterConsistencyCheckStepConfiguration.PHYSICAL_EQUIPMENT_DATA_CENTER_CONSISTENCY_CHECK_STEP_NAME;
import static com.soprasteria.g4it.backend.apibatchloading.steps.physicalequipment.config.ValidatePhysicalEquipmentHeaderStepConfiguration.VALIDATE_PHYSICAL_EQUIPMENT_HEADER_STEP_NAME;
import static com.soprasteria.g4it.backend.apibatchloading.steps.upload.UploadResultStepConfiguration.UPLOAD_STEP_NAME;
import static com.soprasteria.g4it.backend.apibatchloading.steps.upload.tasklet.UploadResultTasklet.FILE_LENGTH_CONTEXT_KEY;
import static com.soprasteria.g4it.backend.apibatchloading.steps.upload.tasklet.UploadResultTasklet.FILE_URL_CONTEXT_KEY;
import static com.soprasteria.g4it.backend.apibatchloading.steps.virtualequipment.config.FlagVirtualEquipmentDataStepConfiguration.FLAG_VIRTUAL_EQUIPMENT_STEP_NAME;
import static com.soprasteria.g4it.backend.apibatchloading.steps.virtualequipment.config.VirtualEquipmentPhysicalEquipmentConsistencyCheckStepConfiguration.VIRTUAL_EQUIPMENT_PHYSICAL_EQUIPMENT_CONSISTENCY_CHECK_STEP_NAME;


/**
 * Inventory Job Execution Listener.
 */
@Slf4j
@RequiredArgsConstructor
public class InventoryJobExecutionListener implements JobExecutionListener {

    /**
     * Inventory repository to update inventory after job.
     */
    private final InventoryRepository inventoryRepository;

    /**
     * Physical equipment repository to count physical equipment.
     */
    private final PhysicalEquipmentRepository physicalEquipmentRepository;

    /**
     * Application repository to count distinct application.
     */
    private final ApplicationRepository applicationRepository;

    /**
     * InVirtualEquipment repository to count distinct inVirtual equipments.
     */
    private final InVirtualEquipmentRepository inVirtualEquipmentRepository;

    /**
     * InApplication repository to count distinct inApplication.
     */
    private final InApplicationRepository inApplicationRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public void beforeJob(final JobExecution jobExecution) {
        log.info("Adding session path to execution context");
        final Date sessionDate = jobExecution.getJobParameters().getDate("session.date");
        jobExecution.getExecutionContext().putString("session.path",
                new SimpleDateFormat(InventoryLoadingSession.SESSION_PATH_FORMAT).format(sessionDate));

        final Inventory processedInventory = inventoryRepository.findById(Objects.requireNonNull(jobExecution.getJobParameters().getLong("inventory.id"))).orElseThrow();
        processedInventory.addIntegrationReport(InventoryIntegrationReport.builder()
                .batchName(jobExecution.getJobParameters().getString("batch.name"))
                .createTime(jobExecution.getCreateTime())
                .batchStatusCode(BatchStatus.STARTED.name())
                .build());
        inventoryRepository.save(processedInventory);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void afterJob(final JobExecution jobExecution) {
        log.info("Step Executions Information");

        final Map<String, StepExecution> stepExecutionByName = jobExecution.getStepExecutions().stream().collect(Collectors.toMap(StepExecution::getStepName, Function.identity()));

        if (headerCheckInFailed(stepExecutionByName)) {
            // Headers failed.
            jobExecution.setExitStatus(new CustomExitStatus("FAILED_HEADERS", ""));
        } else {
            final boolean surfacingControlFail = flagInputFileContainsErrors(stepExecutionByName);
            final boolean consistencyControl = consistencyCheckContainsErrors(stepExecutionByName);
            if (surfacingControlFail || consistencyControl) {
                // Check if validation errors occurs during steps (surfacing control and consistency control).
                jobExecution.setExitStatus(CustomExitStatus.COMPLETED_WITH_ERRORS);
            }
        }

        // Update.
        final Inventory processedInventory = inventoryRepository.findById(Objects.requireNonNull(jobExecution.getJobParameters().getLong("inventory.id"))).orElseThrow();
        final InventoryIntegrationReport currentReport = processedInventory.getIntegrationReports().stream().filter(report -> report.getBatchName().equals(jobExecution.getJobParameters().getString("batch.name"))).findFirst().orElseThrow();
        currentReport.setEndTime(jobExecution.getEndTime());
        currentReport.setBatchStatusCode(jobExecution.getExitStatus().getExitCode());
        jobExecution.getStepExecutions().stream()
                .filter(step -> UPLOAD_STEP_NAME.equals(step.getStepName()))
                .map(StepExecution::getExecutionContext)
                .findFirst().ifPresent(e -> updateWithUploadData(e, currentReport));
        processedInventory.setDataCenterCount((long) Hibernate.size(processedInventory.getDataCenterList()));
        processedInventory.setVirtualEquipmentCount(
                (long) Hibernate.size(processedInventory.getVirtualEquipments()) +
                        inVirtualEquipmentRepository.countQuantityByDistinctNameByInventoryId(processedInventory.getId()));
        processedInventory.setApplicationCount(applicationRepository.countDistinctCloudAndNonCloudApp(processedInventory.getId()));
        processedInventory.setPhysicalEquipmentCount(physicalEquipmentRepository.countByInventoryId(processedInventory.getId()));
        inventoryRepository.save(processedInventory);
    }

    /**
     * Update the report with uploaded file information.
     *
     * @param uploadExecutionContext upload step execution context.
     * @param currentReport          the report to update.
     */
    private void updateWithUploadData(final ExecutionContext uploadExecutionContext, final InventoryIntegrationReport currentReport) {
        if (uploadExecutionContext.containsKey(FILE_URL_CONTEXT_KEY) && uploadExecutionContext.containsKey(FILE_LENGTH_CONTEXT_KEY)) {
            currentReport.setResultFileUrl(uploadExecutionContext.getString(FILE_URL_CONTEXT_KEY));
            currentReport.setResultFileSize(uploadExecutionContext.getLong(FILE_LENGTH_CONTEXT_KEY));
        }
    }

    /**
     * Method to check in all validate headers step that an error occurs.
     *
     * @param stepExecutionByName map containing [Step name - StepExecution data]
     * @return true if error occurs.
     */
    private boolean headerCheckInFailed(Map<String, StepExecution> stepExecutionByName) {
        return Stream.of(hasHeaderCheckFailed(stepExecutionByName.get(VALIDATE_DATACENTER_HEADER_STEP_NAME)),
                        hasHeaderCheckFailed(stepExecutionByName.get(VALIDATE_PHYSICAL_EQUIPMENT_HEADER_STEP_NAME)),
                        hasHeaderCheckFailed(stepExecutionByName.get(VALIDATE_DATACENTER_HEADER_STEP_NAME)),
                        hasHeaderCheckFailed(stepExecutionByName.get(VALIDATE_DATACENTER_HEADER_STEP_NAME)))
                .toList().contains(true);
    }

    /**
     * Method to check in all surfacing control step that an error occurs.
     *
     * @param stepExecutionByName map containing [Step name - StepExecution data]
     * @return true if error occurs.
     */
    private boolean flagInputFileContainsErrors(final Map<String, StepExecution> stepExecutionByName) {
        return Stream.of(
                        errorOccursDuringSurfaceControl(stepExecutionByName.get(FLAG_DATACENTER_STEP_NAME), "unvalidatedDataCenterItemWriter.written"),
                        errorOccursDuringSurfaceControl(stepExecutionByName.get(FLAG_PHYSICAL_EQUIPMENT_STEP_NAME), "unvalidatedPhysicalEquipmentItemWriter.written"),
                        errorOccursDuringSurfaceControl(stepExecutionByName.get(FLAG_VIRTUAL_EQUIPMENT_STEP_NAME), "unvalidatedVirtualEquipmentItemWriter.written"),
                        errorOccursDuringSurfaceControl(stepExecutionByName.get(FLAG_APPLICATION_STEP_NAME), "unvalidatedApplicationItemWriter.written"))
                .toList().contains(true);
    }

    /**
     * Method to check in all consistency check step if error occurs.
     *
     * @param stepExecutionByName map containing [Step name - StepExecution data]
     * @return true if error occurs.
     */
    private boolean consistencyCheckContainsErrors(final Map<String, StepExecution> stepExecutionByName) {
        return Stream.of(errorOccursDuringConsistencyControl(stepExecutionByName.get(PHYSICAL_EQUIPMENT_DATA_CENTER_CONSISTENCY_CHECK_STEP_NAME)),
                        errorOccursDuringConsistencyControl(stepExecutionByName.get(VIRTUAL_EQUIPMENT_PHYSICAL_EQUIPMENT_CONSISTENCY_CHECK_STEP_NAME)),
                        errorOccursDuringConsistencyControl(stepExecutionByName.get(APPLICATION_VIRTUAL_EQUIPMENT_CONSISTENCY_CHECK_STEP_NAME)))
                .toList().contains(true);
    }

    /**
     * Method to check in one headers check step that an error occurs and log it.
     *
     * @param stepExecution map containing [Step name - StepExecution data]
     * @return true if error occurs.
     */
    private boolean hasHeaderCheckFailed(final StepExecution stepExecution) {
        if (stepExecution != null && stepExecution.getExitStatus().getExitCode().equals("SKIPPED")) {
            log.warn("Headers error in step {}", stepExecution.getStepName());
            return true;
        }
        return false;
    }

    /**
     * Method to check within one surfacing control step that an error occurs and log it.
     *
     * @param stepExecution step execution data.
     * @return true if error occurs.
     */
    private boolean errorOccursDuringSurfaceControl(final StepExecution stepExecution, final String parameterName) {
        if (stepExecution != null) {
            log.info("Step {} - {} lines have been read.", stepExecution.getStepName(), stepExecution.getReadCount());
            log.info("Step {} - {} lines are duplicates", stepExecution.getStepName(), stepExecution.getWriteSkipCount());
            if (stepExecution.getExecutionContext().containsKey(parameterName) && stepExecution.getExecutionContext().getLong(parameterName) != 0L) {
                log.warn("An error occurred during surface control ({}), {} items were rejected", stepExecution.getStepName(), stepExecution.getExecutionContext().getLong(parameterName));
                return true;
            }
        }
        return false;
    }

    /**
     * Method to check within one consistency check step that an error occurs and log it.
     *
     * @param stepExecution step execution data.
     * @return true if error occurs.
     */
    private boolean errorOccursDuringConsistencyControl(final StepExecution stepExecution) {
        if (stepExecution != null && stepExecution.getWriteCount() > 0L) {
            log.warn("An error occurred during consistency check control ({}), {} were rejected", stepExecution.getStepName(), stepExecution.getWriteCount());
            return true;
        }
        return false;
    }

}

