/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apibatchloading.steps.virtualequipment.config;

import com.soprasteria.g4it.backend.apibatchloading.steps.common.classifier.FlaggedDataWriterClassifier;
import com.soprasteria.g4it.backend.apibatchloading.steps.common.config.AbstractFlagDataStepConfiguration;
import com.soprasteria.g4it.backend.apibatchloading.steps.common.mapper.CsvLineMapper;
import com.soprasteria.g4it.backend.apibatchloading.steps.virtualequipment.listener.FlagVirtualEquipmentDataWriterListener;
import com.soprasteria.g4it.backend.apibatchloading.steps.virtualequipment.processor.VirtualEquipmentItemProcessor;
import com.soprasteria.g4it.backend.apibatchloading.utils.QuotelessLineTokenizer;
import com.soprasteria.g4it.backend.apiinventory.mapper.VirtualEquipmentMapper;
import com.soprasteria.g4it.backend.apiinventory.modeldb.VirtualEquipment;
import com.soprasteria.g4it.backend.apiinventory.repository.VirtualEquipmentRepository;
import com.soprasteria.g4it.backend.common.filesystem.business.FileStorage;
import com.soprasteria.g4it.backend.common.filesystem.model.FileFolder;
import com.soprasteria.g4it.backend.common.filesystem.model.FileMapperInfo;
import com.soprasteria.g4it.backend.common.filesystem.model.FileType;
import com.soprasteria.g4it.backend.common.filesystem.model.Header;
import jakarta.validation.Validator;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.skip.AlwaysSkipItemSkipPolicy;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.builder.MultiResourceItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.support.ClassifierCompositeItemWriter;
import org.springframework.batch.item.support.builder.ClassifierCompositeItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.FileSystemResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Flag Virtual Equipment data step configuration.
 */
@Configuration
public class FlagVirtualEquipmentDataStepConfiguration extends AbstractFlagDataStepConfiguration {

    /**
     * The step name.
     */
    public static final String FLAG_VIRTUAL_EQUIPMENT_STEP_NAME = "flagVirtualEquipmentDataStep";

    /**
     * Step definition.
     *
     * @param jobRepository                                 Spring Batch Job Repository.
     * @param transactionManager                            the transaction manager (since Spring Batch v5).
     * @param multiVirtualEquipmentResourceItemReader       Spring MultiResourceItemReader to read virtual equipment input files.
     * @param validateVirtualEquipmentProcessor             processor to flag data.
     * @param virtualEquipmentClassifierCompositeItemWriter Spring composite writer to write data.
     * @param unvalidatedVirtualEquipmentItemWriter         Spring FlatFileItemWriter to stream to open.
     * @param flagVirtualEquipmentDataWriterListener        Spring ItemWriterListener to process duplicates.
     * @param chunkValue                                    chunk size.
     * @return the configured Step.
     */
    @Bean
    public Step flagVirtualEquipmentDataStep(final JobRepository jobRepository,
                                             final PlatformTransactionManager transactionManager,
                                             final MultiResourceItemReader<VirtualEquipment> multiVirtualEquipmentResourceItemReader,
                                             final VirtualEquipmentItemProcessor validateVirtualEquipmentProcessor,
                                             final ClassifierCompositeItemWriter<VirtualEquipment> virtualEquipmentClassifierCompositeItemWriter,
                                             final FlatFileItemWriter<VirtualEquipment> unvalidatedVirtualEquipmentItemWriter,
                                             final FlagVirtualEquipmentDataWriterListener flagVirtualEquipmentDataWriterListener,
                                             @Value("${loading.batch.chunk}") final Integer chunkValue) {
        return new StepBuilder(FLAG_VIRTUAL_EQUIPMENT_STEP_NAME, jobRepository)
                .<VirtualEquipment, VirtualEquipment>chunk(chunkValue, transactionManager)
                .reader(multiVirtualEquipmentResourceItemReader)
                .processor(validateVirtualEquipmentProcessor)
                .writer(virtualEquipmentClassifierCompositeItemWriter)
                .listener((ItemWriteListener<? super VirtualEquipment>) flagVirtualEquipmentDataWriterListener)
                .faultTolerant().skip(DataIntegrityViolationException.class).skipPolicy(new AlwaysSkipItemSkipPolicy())
                .stream(unvalidatedVirtualEquipmentItemWriter)
                .build();
    }

    /**
     * Multi VirtualEquipment resource reader configuration.
     *
     * @param sessionPath                           the session date formatted (subfolder).
     * @param readVirtualEquipmentWorkingFileReader the delegate reader.
     * @param loadingFileStorage                    to access resource file.
     * @return the configured multi resource reader.
     */
    @Bean
    @StepScope
    public MultiResourceItemReader<VirtualEquipment> multiVirtualEquipmentResourceItemReader(@Value("#{jobExecutionContext['session.path']}") final String sessionPath,
                                                                                             final FlatFileItemReader<VirtualEquipment> readVirtualEquipmentWorkingFileReader,
                                                                                             final FileStorage loadingFileStorage) throws IOException {
        return new MultiResourceItemReaderBuilder<VirtualEquipment>()
                .name("multiVirtualEquipmentResourceItemReader")
                .resources(loadingFileStorage.listResources(FileFolder.WORK, sessionPath, FileType.EQUIPEMENT_VIRTUEL))
                .delegate(readVirtualEquipmentWorkingFileReader)
                .build();
    }

    /**
     * Virtual equipment FlatFileItemReader definition.
     *
     * @param fileInfo to access input file information.
     * @return the configured Reader.
     */
    @Bean
    @StepScope
    public FlatFileItemReader<VirtualEquipment> readVirtualEquipmentWorkingFileReader(final FileMapperInfo fileInfo) {
        final CsvLineMapper<VirtualEquipment> mapper = new CsvLineMapper<>();
        final DelimitedLineTokenizer tokenizer = new QuotelessLineTokenizer();
        tokenizer.setDelimiter(";");
        mapper.setLineTokenizer(tokenizer);
        final BeanWrapperFieldSetMapper<VirtualEquipment> beanWrapper = new BeanWrapperFieldSetMapper<>();
        beanWrapper.setTargetType(VirtualEquipment.class);
        mapper.setFieldSetMapper(beanWrapper);
        return new FlatFileItemReaderBuilder<VirtualEquipment>().name("readVirtualEquipmentWorkingFileReader")
                .lineMapper(mapper)
                .strict(false)
                .linesToSkip(1)
                .skippedLinesCallback(line -> skippedLinesCallback(FileType.EQUIPEMENT_VIRTUEL, line, tokenizer, fileInfo))
                .build();
    }

    /**
     * Virtual equipment processor definition.
     *
     * @param inventoryId      the inventory unique identifier.
     * @param sessionDate      the session date.
     * @param validatorFactory the created validator data.
     * @return the configured processor.
     */
    @Bean
    @StepScope
    public VirtualEquipmentItemProcessor validateVirtualEquipmentProcessor(
            @Value("#{jobParameters['inventory.id']}") final long inventoryId,
            @Value("#{jobParameters['session.date']}") final Date sessionDate,
            @Value("#{jobParameters['locale']}") final Locale locale,
            final Validator validatorFactory) {
        LocaleContextHolder.setLocale(locale);
        return new VirtualEquipmentItemProcessor(sessionDate, inventoryId, validatorFactory);
    }

    /**
     * Classifier composite item writer configuration to write the valid data to the database, and the bad data to a reject file.
     *
     * @param flagVirtualEquipmentWriterClassifier the custom classifier based on 'valid' attribut.
     * @return the configured writer.
     */
    @Bean
    public ClassifierCompositeItemWriter<VirtualEquipment> virtualEquipmentClassifierCompositeItemWriter(final FlaggedDataWriterClassifier<VirtualEquipment> flagVirtualEquipmentWriterClassifier) {
        return new ClassifierCompositeItemWriterBuilder<VirtualEquipment>().classifier(flagVirtualEquipmentWriterClassifier).build();
    }

    /**
     * Classifier configuration, based on 'valid' attribut.
     *
     * @param persistVirtualEquipmentWriter         the spring writer to database.
     * @param unvalidatedVirtualEquipmentItemWriter the spring writer to reject file.
     * @return the configured classifier.
     */
    @Bean
    public FlaggedDataWriterClassifier<VirtualEquipment> flagVirtualEquipmentWriterClassifier(
            final RepositoryItemWriter<VirtualEquipment> persistVirtualEquipmentWriter, final FlatFileItemWriter<VirtualEquipment> unvalidatedVirtualEquipmentItemWriter) {
        return new FlaggedDataWriterClassifier<>(persistVirtualEquipmentWriter, unvalidatedVirtualEquipmentItemWriter);
    }

    /**
     * Virtual equipment RepositoryItemWriter definition to write good data in database.
     *
     * @param virtualEquipmentRepository repository to write data in database.
     * @return the configured writer.
     */
    @Bean
    public RepositoryItemWriter<VirtualEquipment> persistVirtualEquipmentWriter(final VirtualEquipmentRepository virtualEquipmentRepository) {
        final RepositoryItemWriterBuilder<VirtualEquipment> builder = new RepositoryItemWriterBuilder<>();
        return builder.repository(virtualEquipmentRepository).methodName("save").build();
    }

    /**
     * Unvalidated Virtual equipment FlatFileItemWriter definition to write bad data in reject file.
     *
     * @param sessionPath        the formatted session date.
     * @param localWorkingFolder generated local working folder
     * @param fileInfo           to get file's information tool.
     * @return the configured writer.
     */
    @Bean
    @StepScope
    public FlatFileItemWriter<VirtualEquipment> unvalidatedVirtualEquipmentItemWriter(
            @Value("#{jobExecutionContext['session.path']}") final String sessionPath,
            @Value("#{jobParameters['local.working.folder']}") final String localWorkingFolder,
            final FileMapperInfo fileInfo) {
        final List<String> headers = new ArrayList<>(fileInfo.getMapping(FileType.EQUIPEMENT_VIRTUEL).stream().map(Header::getName).toList());
        headers.add("inputFileName");
        headers.add("lineNumber");
        headers.add("message");
        return new FlatFileItemWriterBuilder<VirtualEquipment>()
                .name("unvalidatedVirtualEquipmentItemWriter")
                .resource(new FileSystemResource(Path.of(localWorkingFolder, String.join("", "rejected_virtual_equipment_", sessionPath, ".csv"))))
                .delimited()
                .delimiter(";")
                .names(headers.toArray(String[]::new))
                .headerCallback(writer -> writer.write(String.join(";", headers)))
                .build();
    }

    /**
     * Item Writer listener to process duplicates.
     *
     * @param virtualEquipmentRepository the repository to update data.
     * @return the configured listener.
     */
    @Bean
    public FlagVirtualEquipmentDataWriterListener flagVirtualEquipmentDataWriterListener(final VirtualEquipmentRepository virtualEquipmentRepository) {
        return new FlagVirtualEquipmentDataWriterListener(virtualEquipmentRepository, VirtualEquipmentMapper.INSTANCE);
    }
}
