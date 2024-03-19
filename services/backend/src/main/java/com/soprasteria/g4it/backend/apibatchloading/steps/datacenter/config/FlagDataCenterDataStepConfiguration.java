/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apibatchloading.steps.datacenter.config;

import com.soprasteria.g4it.backend.apibatchloading.steps.common.classifier.FlaggedDataWriterClassifier;
import com.soprasteria.g4it.backend.apibatchloading.steps.common.config.AbstractFlagDataStepConfiguration;
import com.soprasteria.g4it.backend.apibatchloading.steps.common.config.LoadReferentialStepConfiguration;
import com.soprasteria.g4it.backend.apibatchloading.steps.common.mapper.CsvLineMapper;
import com.soprasteria.g4it.backend.apibatchloading.steps.datacenter.listener.FlagDataCenterDataWriterListener;
import com.soprasteria.g4it.backend.apibatchloading.steps.datacenter.processor.DataCenterItemProcessor;
import com.soprasteria.g4it.backend.apibatchloading.utils.QuotelessLineTokenizer;
import com.soprasteria.g4it.backend.apiinventory.mapper.DataCenterMapper;
import com.soprasteria.g4it.backend.apiinventory.modeldb.DataCenter;
import com.soprasteria.g4it.backend.apiinventory.repository.DataCenterRepository;
import com.soprasteria.g4it.backend.common.filesystem.model.*;
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
 * Flag DataCenter data step configuration.
 */
@Configuration
public class FlagDataCenterDataStepConfiguration extends AbstractFlagDataStepConfiguration {

    /**
     * The step name.
     */
    public static final String FLAG_DATACENTER_STEP_NAME = "flagDataCenterDataStep";

    /**
     * NumEcoEval Referential
     */
    @Autowired
    private NumEcoEvalReferentialRemotingService numEcoEvalReferentialRemotingService;

    /**
     * Step definition.
     *
     * @param jobRepository                           Spring Batch Job Repository.
     * @param transactionManager                      the transaction manager (since Spring Batch v5).
     * @param multiDataCenterResourceItemReader       Spring MultiResourceItemReader to read input DataCenter working files.
     * @param validateDataCenterProcessor             processor to flag data.
     * @param dataCenterClassifierCompositeItemWriter Spring composite writer to write data.
     * @param unvalidatedDataCenterItemWriter         Spring FlatFileItemWriter to stream to open.
     * @param flagDataCenterDataWriterListener        Spring itemWriter listener to process duplicates.
     * @param chunkValue                              chunk size.
     * @return the configured Step.
     */
    @Bean
    public Step flagDataCenterDataStep(final JobRepository jobRepository,
                                       final PlatformTransactionManager transactionManager,
                                       final MultiResourceItemReader<DataCenter> multiDataCenterResourceItemReader,
                                       final DataCenterItemProcessor validateDataCenterProcessor,
                                       final ClassifierCompositeItemWriter<DataCenter> dataCenterClassifierCompositeItemWriter,
                                       final FlatFileItemWriter<DataCenter> unvalidatedDataCenterItemWriter,
                                       final FlagDataCenterDataWriterListener flagDataCenterDataWriterListener,
                                       @Value("${loading.batch.chunk}") final Integer chunkValue) {
        return new StepBuilder(FLAG_DATACENTER_STEP_NAME, jobRepository).
                <DataCenter, DataCenter>chunk(chunkValue, transactionManager)
                .reader(multiDataCenterResourceItemReader)
                .processor(validateDataCenterProcessor)
                .writer(dataCenterClassifierCompositeItemWriter)
                .listener((ItemWriteListener<? super DataCenter>) flagDataCenterDataWriterListener)
                .faultTolerant().skip(DataIntegrityViolationException.class).skipPolicy(new AlwaysSkipItemSkipPolicy())
                .stream(unvalidatedDataCenterItemWriter)
                .build();
    }

    /**
     * Multi DataCenter resource reader configuration.
     *
     * @param sessionPath                     the session date formatted (subfolder).
     * @param readDataCenterWorkingFileReader the delegate reader.
     * @param loadingFileStorage              to access resource file.
     * @return the configured multi resource reader.
     */
    @Bean
    @StepScope
    public MultiResourceItemReader<DataCenter> multiDataCenterResourceItemReader(@Value("#{jobExecutionContext['session.path']}") final String sessionPath,
                                                                                 final FlatFileItemReader<DataCenter> readDataCenterWorkingFileReader,
                                                                                 final FileStorage loadingFileStorage) throws IOException {
        return new MultiResourceItemReaderBuilder<DataCenter>()
                .name("multiDataCenterResourceItemReader")
                .resources(loadingFileStorage.listResources(FileFolder.WORK, sessionPath, FileType.DATACENTER))
                .delegate(readDataCenterWorkingFileReader)
                .build();
    }

    /**
     * DataCenter FlatFileItemReader definition.
     *
     * @return the configured Reader.
     */
    @Bean
    public FlatFileItemReader<DataCenter> readDataCenterWorkingFileReader(final FileMapperInfo fileInfo) {
        final CsvLineMapper<DataCenter> mapper = new CsvLineMapper<>();
        final DelimitedLineTokenizer tokenizer = new QuotelessLineTokenizer();
        tokenizer.setDelimiter(";");
        mapper.setLineTokenizer(tokenizer);
        final BeanWrapperFieldSetMapper<DataCenter> beanWrapper = new BeanWrapperFieldSetMapper<>();
        beanWrapper.setTargetType(DataCenter.class);
        mapper.setFieldSetMapper(beanWrapper);
        return new FlatFileItemReaderBuilder<DataCenter>().name("readDataCenterWorkingFileReader")
                .lineMapper(mapper)
                .strict(false)
                .linesToSkip(1)
                .skippedLinesCallback(line -> skippedLinesCallback(FileType.DATACENTER, line, tokenizer, fileInfo))
                .build();
    }

    /**
     * DataCenter data processor definition.
     *
     * @param sessionDate          the session date.
     * @param inventoryId          the inventory unique identifier.
     * @param countriesReferential countries from numEcoEval.
     * @param messageSource        internationalization message.
     * @param validatorFactory     the created validator.
     * @return the configured processor.
     */
    @Bean
    @StepScope
    public DataCenterItemProcessor validateDataCenterProcessor(
            @Value("#{jobParameters['session.date']}") final Date sessionDate,
            @Value("#{jobParameters['inventory.id']}") final long inventoryId,
            @Value("#{jobParameters['locale']}") final Locale locale,
            @Value("#{jobExecutionContext['" + LoadReferentialStepConfiguration.COUNTRIES_CONTEXT_KEY + "']}") final List<String> countriesReferential,
            final MessageSource messageSource,
            final Validator validatorFactory) {
        LocaleContextHolder.setLocale(locale);
        return new DataCenterItemProcessor(sessionDate, inventoryId, validatorFactory, messageSource, countriesReferential, locale);
    }

    /**
     * Classifier composite item writer configuration to write the valid data to the database, and the bad data to a reject file.
     *
     * @param flagDataCenterWriterClassifier the custom classifier based on 'valid' attribut.
     * @return the configured writer.
     */
    @Bean
    public ClassifierCompositeItemWriter<DataCenter> dataCenterClassifierCompositeItemWriter(final FlaggedDataWriterClassifier<DataCenter> flagDataCenterWriterClassifier) {
        return new ClassifierCompositeItemWriterBuilder<DataCenter>().classifier(flagDataCenterWriterClassifier).build();
    }

    /**
     * Classifier configuration, bases on 'valid' attribut.
     *
     * @param persistDataCenterValidationWriter the spring writer to database.
     * @param unvalidatedDataCenterItemWriter   the spring writer to reject file.
     * @return the configured classifier.
     */
    @Bean
    public FlaggedDataWriterClassifier<DataCenter> flagDataCenterWriterClassifier(
            final RepositoryItemWriter<DataCenter> persistDataCenterValidationWriter, final FlatFileItemWriter<DataCenter> unvalidatedDataCenterItemWriter) {
        return new FlaggedDataWriterClassifier<>(persistDataCenterValidationWriter, unvalidatedDataCenterItemWriter);
    }

    /**
     * DataCenter RepositoryItemWriter definition, to write valid data.
     *
     * @param dataCenterRepository repository to write data in database.
     * @return the configured writer.
     */
    @Bean
    public RepositoryItemWriter<DataCenter> persistDataCenterWriter(final DataCenterRepository dataCenterRepository) {
        final RepositoryItemWriterBuilder<DataCenter> builder = new RepositoryItemWriterBuilder<>();
        return builder.repository(dataCenterRepository).methodName("save").build();
    }

    /**
     * DataCenter FlatFileItemWriter definition, to write bad data.
     *
     * @param sessionPath        the formatted session date.
     * @param localWorkingFolder generated local working folder.
     * @param fileInfo           file information.
     * @return the configured writer.
     */
    @Bean
    @StepScope
    public FlatFileItemWriter<DataCenter> unvalidatedDataCenterItemWriter(
            @Value("#{jobExecutionContext['session.path']}") final String sessionPath,
            @Value("#{jobParameters['local.working.folder']}") final String localWorkingFolder,
            final FileMapperInfo fileInfo) {
        final List<String> headers = new ArrayList<>(fileInfo.getMapping(FileType.DATACENTER).stream().map(Header::getName).toList());
        // we add the validation message in the output file
        headers.add("inputFileName");
        headers.add("lineNumber");
        headers.add("message");
        return new FlatFileItemWriterBuilder<DataCenter>()
                .name("unvalidatedDataCenterItemWriter")
                .resource(new FileSystemResource(Path.of(localWorkingFolder, String.join("", "rejected_datacenter_", sessionPath, ".csv"))))
                .delimited().delimiter(";").names(headers.toArray(String[]::new))
                .headerCallback(writer -> writer.write(String.join(";", headers)))
                .build();
    }

    /**
     * Item Writer listener to process duplicates.
     *
     * @param dataCenterRepository the repository to update data.
     * @return the configured listener.
     */
    @Bean
    public FlagDataCenterDataWriterListener flagDataCenterDataWriterListener(final DataCenterRepository dataCenterRepository) {
        return new FlagDataCenterDataWriterListener(dataCenterRepository, DataCenterMapper.INSTANCE);
    }

}
