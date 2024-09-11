/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apibatchloading.steps.physicalequipment.config;

import com.soprasteria.g4it.backend.apibatchloading.steps.common.classifier.FlaggedDataWriterClassifier;
import com.soprasteria.g4it.backend.apibatchloading.steps.common.config.AbstractFlagDataStepConfiguration;
import com.soprasteria.g4it.backend.apibatchloading.steps.common.config.LoadReferentialStepConfiguration;
import com.soprasteria.g4it.backend.apibatchloading.steps.common.mapper.CsvLineMapper;
import com.soprasteria.g4it.backend.apibatchloading.steps.physicalequipment.listener.FlagPhysicalEquipmentDataWriterListener;
import com.soprasteria.g4it.backend.apibatchloading.steps.physicalequipment.processor.PhysicalEquipmentItemProcessor;
import com.soprasteria.g4it.backend.apibatchloading.utils.QuotelessLineTokenizer;
import com.soprasteria.g4it.backend.apiinventory.mapper.PhysicalEquipmentMapper;
import com.soprasteria.g4it.backend.apiinventory.modeldb.PhysicalEquipment;
import com.soprasteria.g4it.backend.apiinventory.repository.PhysicalEquipmentRepository;
import com.soprasteria.g4it.backend.common.filesystem.business.FileStorage;
import com.soprasteria.g4it.backend.common.filesystem.model.FileFolder;
import com.soprasteria.g4it.backend.common.filesystem.model.FileMapperInfo;
import com.soprasteria.g4it.backend.common.filesystem.model.FileType;
import com.soprasteria.g4it.backend.common.filesystem.model.Header;
import com.soprasteria.g4it.backend.external.numecoeval.business.NumEcoEvalReferentialRemotingService;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
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
 * Flag Physical Equipment data step configuration.
 */
@Configuration
public class FlagPhysicalEquipmentDataStepConfiguration extends AbstractFlagDataStepConfiguration {

    /**
     * The step name.
     */
    public static final String FLAG_PHYSICAL_EQUIPMENT_STEP_NAME = "flagPhysicalEquipmentDataStep";

    /**
     * NumEcoEval Referential Remoting Service.
     */
    @Autowired
    private NumEcoEvalReferentialRemotingService numEcoEvalReferentialRemotingService;

    /**
     * Step definition.
     *
     * @param jobRepository                                  Spring Batch Job Repository.
     * @param transactionManager                             the transaction manager (since Spring Batch v5).
     * @param multiPhysicalEquipmentResourceItemReader       Spring MultiResourceItemReader to read physical equipment input files.
     * @param validatePhysicalEquipmentProcessor             processor to flag data.
     * @param physicalEquipmentClassifierCompositeItemWriter Spring composite writer to write data.
     * @param unvalidatedPhysicalEquipmentItemWriter         Spring FlatFileItemWriter to stream to open.
     * @param flagPhysicalEquipmentDataWriterListener        Spring itemWriterListener to process duplicated.
     * @param chunkValue                                     chunk size.
     * @return the configured Step.
     */
    @Bean
    public Step flagPhysicalEquipmentDataStep(final JobRepository jobRepository,
                                              final PlatformTransactionManager transactionManager,
                                              final MultiResourceItemReader<PhysicalEquipment> multiPhysicalEquipmentResourceItemReader,
                                              final PhysicalEquipmentItemProcessor validatePhysicalEquipmentProcessor,
                                              final ClassifierCompositeItemWriter<PhysicalEquipment> physicalEquipmentClassifierCompositeItemWriter,
                                              final FlatFileItemWriter<PhysicalEquipment> unvalidatedPhysicalEquipmentItemWriter,
                                              final FlagPhysicalEquipmentDataWriterListener flagPhysicalEquipmentDataWriterListener,
                                              @Value("${loading.batch.chunk}") final Integer chunkValue) {
        return new StepBuilder(FLAG_PHYSICAL_EQUIPMENT_STEP_NAME, jobRepository)
                .<PhysicalEquipment, PhysicalEquipment>chunk(chunkValue, transactionManager)
                .reader(multiPhysicalEquipmentResourceItemReader).processor(validatePhysicalEquipmentProcessor)
                .writer(physicalEquipmentClassifierCompositeItemWriter)
                .listener((ItemWriteListener<? super PhysicalEquipment>) flagPhysicalEquipmentDataWriterListener)
                .faultTolerant().skip(DataIntegrityViolationException.class).skipPolicy(new AlwaysSkipItemSkipPolicy())
                .stream(unvalidatedPhysicalEquipmentItemWriter)
                .build();
    }

    /**
     * Multi PhysicalEquipment resource reader configuration.
     *
     * @param sessionPath                            the session date formatted (subfolder).
     * @param readPhysicalEquipmentWorkingFileReader the delegate reader.
     * @param loadingFileStorage                     to access resource file.
     * @return the configured multi resource reader.
     */
    @Bean
    @StepScope
    public MultiResourceItemReader<PhysicalEquipment> multiPhysicalEquipmentResourceItemReader(@Value("#{jobExecutionContext['session.path']}") final String sessionPath,
                                                                                               final FlatFileItemReader<PhysicalEquipment> readPhysicalEquipmentWorkingFileReader,
                                                                                               final FileStorage loadingFileStorage) throws IOException {
        return new MultiResourceItemReaderBuilder<PhysicalEquipment>()
                .name("multiPhysicalEquipmentResourceItemReader")
                .resources(loadingFileStorage.listResources(FileFolder.WORK, sessionPath, FileType.EQUIPEMENT_PHYSIQUE))
                .delegate(readPhysicalEquipmentWorkingFileReader)
                .build();
    }

    /**
     * Physical equipment FlatFileItemReader definition.
     *
     * @param fileInfo to access input file information.
     * @return the configured Reader.
     */
    @Bean
    @StepScope
    public FlatFileItemReader<PhysicalEquipment> readPhysicalEquipmentWorkingFileReader(final FileMapperInfo fileInfo) {
        final CsvLineMapper<PhysicalEquipment> mapper = new CsvLineMapper<>();
        final DelimitedLineTokenizer tokenizer = new QuotelessLineTokenizer();
        tokenizer.setDelimiter(";");
        mapper.setLineTokenizer(tokenizer);
        final BeanWrapperFieldSetMapper<PhysicalEquipment> beanWrapper = new BeanWrapperFieldSetMapper<>();
        beanWrapper.setTargetType(PhysicalEquipment.class);
        mapper.setFieldSetMapper(beanWrapper);
        return new FlatFileItemReaderBuilder<PhysicalEquipment>().name("readPhysicalEquipmentWorkingFileReader")
                .lineMapper(mapper)
                .strict(false)
                .linesToSkip(1)
                .skippedLinesCallback(line -> skippedLinesCallback(FileType.EQUIPEMENT_PHYSIQUE, line, tokenizer, fileInfo))
                .build();
    }

    /**
     * Physical equipment processor definition.
     *
     * @param inventoryId               inventory unique identifier.
     * @param sessionDate               the session date.
     * @param countriesReferential      countries from numEcoEval.
     * @param equipmentTypesReferential equipment types from numEcoEval.
     * @param messageSource             the internationalisation messages.
     * @param validatorFactory          the created validator.
     * @return the configured processor.
     */
    @Bean
    @StepScope
    public PhysicalEquipmentItemProcessor validatePhysicalEquipmentProcessor(
            @Value("#{jobParameters['inventory.id']}") final long inventoryId,
            @Value("#{jobParameters['session.date']}") final Date sessionDate,
            @Value("#{jobParameters['locale']}") final Locale locale,
            @Value("#{jobExecutionContext['" + LoadReferentialStepConfiguration.COUNTRIES_CONTEXT_KEY + "']}") final List<String> countriesReferential,
            @Value("#{jobExecutionContext['" + LoadReferentialStepConfiguration.EQUIPMENT_TYPES_CONTEXT_KEY + "']}") final List<String> equipmentTypesReferential,
            final MessageSource messageSource,
            final Validator validatorFactory) {
        LocaleContextHolder.setLocale(locale);
        return new PhysicalEquipmentItemProcessor(sessionDate, inventoryId, validatorFactory, messageSource, countriesReferential, equipmentTypesReferential, locale);
    }

    /**
     * Classifier composite item writer configuration to write the valid data to the database, and the bad data to a reject file.
     *
     * @param flagPhysicalEquipmentWriterClassifier the custom classifier based on 'valid' attribut.
     * @return the configured writer.
     */
    @Bean
    public ClassifierCompositeItemWriter<PhysicalEquipment> physicalEquipmentClassifierCompositeItemWriter(final FlaggedDataWriterClassifier<PhysicalEquipment> flagPhysicalEquipmentWriterClassifier) {
        return new ClassifierCompositeItemWriterBuilder<PhysicalEquipment>().classifier(flagPhysicalEquipmentWriterClassifier).build();
    }

    /**
     * Classifier configuration, bases on 'valid' attribut.
     *
     * @param persistPhysicalEquipmentWriter         the spring writer to database.
     * @param unvalidatedPhysicalEquipmentItemWriter the spring writer to reject file.
     * @return the configured classifier.
     */
    @Bean
    public FlaggedDataWriterClassifier<PhysicalEquipment> flagPhysicalEquipmentWriterClassifier(
            final RepositoryItemWriter<PhysicalEquipment> persistPhysicalEquipmentWriter, final FlatFileItemWriter<PhysicalEquipment> unvalidatedPhysicalEquipmentItemWriter) {
        return new FlaggedDataWriterClassifier<>(persistPhysicalEquipmentWriter, unvalidatedPhysicalEquipmentItemWriter);
    }

    /**
     * Physical equipment RepositoryItemWriter definition to write good data in database.
     *
     * @param physicalEquipmentRepository repository to write data in database.
     * @return the configured writer.
     */
    @Bean
    public RepositoryItemWriter<PhysicalEquipment> persistPhysicalEquipmentWriter(final PhysicalEquipmentRepository physicalEquipmentRepository) {
        final RepositoryItemWriterBuilder<PhysicalEquipment> builder = new RepositoryItemWriterBuilder<>();
        return builder.repository(physicalEquipmentRepository).methodName("save").build();
    }

    /**
     * Unvalidated Physical equipment FlatFileItemWriter definition to write bad data in reject file.
     *
     * @param sessionPath        the formatted session date.
     * @param localWorkingFolder generated local working folder
     * @param fileInfo           to get file's information tool.
     * @return the configured writer.
     */
    @Bean
    @StepScope
    public FlatFileItemWriter<PhysicalEquipment> unvalidatedPhysicalEquipmentItemWriter(
            @Value("#{jobExecutionContext['session.path']}") final String sessionPath,
            @Value("#{jobParameters['local.working.folder']}") final String localWorkingFolder,
            final FileMapperInfo fileInfo) {
        final List<String> headers = new ArrayList<>(fileInfo.getMapping(FileType.EQUIPEMENT_PHYSIQUE).stream().map(Header::getName).toList());
        headers.add("inputFileName");
        headers.add("lineNumber");
        headers.add("message");
        return new FlatFileItemWriterBuilder<PhysicalEquipment>()
                .name("unvalidatedPhysicalEquipmentItemWriter")
                .resource(new FileSystemResource(Path.of(localWorkingFolder, String.join("", "rejected_physical_equipment_", sessionPath, ".csv"))))
                .delimited().delimiter(";").names(headers.toArray(String[]::new))
                .headerCallback(writer -> writer.write(String.join(";", headers)))
                .build();
    }

    /**
     * Item Writer listener to process duplicates.
     *
     * @param physicalEquipmentRepository the repository to update data.
     * @return the configured listener.
     */
    @Bean
    public FlagPhysicalEquipmentDataWriterListener flagPhysicalEquipmentDataWriterListener(final PhysicalEquipmentRepository physicalEquipmentRepository) {
        return new FlagPhysicalEquipmentDataWriterListener(physicalEquipmentRepository, PhysicalEquipmentMapper.INSTANCE);
    }
}
