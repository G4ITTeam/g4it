/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apibatchloading.steps.application.config;

import com.soprasteria.g4it.backend.apibatchloading.steps.application.listener.FlagApplicationDataWriterListener;
import com.soprasteria.g4it.backend.apibatchloading.steps.application.processor.ApplicationItemProcessor;
import com.soprasteria.g4it.backend.apibatchloading.steps.common.classifier.FlaggedDataWriterClassifier;
import com.soprasteria.g4it.backend.apibatchloading.steps.common.config.AbstractFlagDataStepConfiguration;
import com.soprasteria.g4it.backend.apibatchloading.steps.common.mapper.CsvLineMapper;
import com.soprasteria.g4it.backend.apibatchloading.utils.QuotelessLineTokenizer;
import com.soprasteria.g4it.backend.apiinventory.mapper.ApplicationMapper;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Application;
import com.soprasteria.g4it.backend.apiinventory.repository.ApplicationRepository;
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
 * Flag Application data step configuration.
 */
@Configuration
public class FlagApplicationDataStepConfiguration extends AbstractFlagDataStepConfiguration {

    /**
     * The step name.
     */
    public static final String FLAG_APPLICATION_STEP_NAME = "flagApplicationDataStep";

    /**
     * Step definition.
     *
     * @param jobRepository                            Spring Batch Job Repository.
     * @param transactionManager                       the transaction manager (since Spring Batch v5).
     * @param multiApplicationResourceItemReader       Spring MultiResourceItemReader to read application input files.
     * @param validateApplicationProcessor             processor to flag data.
     * @param applicationClassifierCompositeItemWriter Spring composite writer to write data.
     * @param unvalidatedApplicationItemWriter         Spring FlatFileItemWriter to stream to open.
     * @param flagApplicationDataWriterListener        Spring ItemWriterListener to process duplicates.
     * @param chunkValue                               chunk size.
     * @return the configured Step.
     */
    @Bean
    public Step flagApplicationDataStep(final JobRepository jobRepository,
                                        final PlatformTransactionManager transactionManager,
                                        final MultiResourceItemReader<Application> multiApplicationResourceItemReader,
                                        final ApplicationItemProcessor validateApplicationProcessor,
                                        final ClassifierCompositeItemWriter<Application> applicationClassifierCompositeItemWriter,
                                        final FlatFileItemWriter<Application> unvalidatedApplicationItemWriter,
                                        final FlagApplicationDataWriterListener flagApplicationDataWriterListener,
                                        @Value("${loading.batch.chunk}") final Integer chunkValue) {
        return new StepBuilder(FLAG_APPLICATION_STEP_NAME, jobRepository)
                .<Application, Application>chunk(chunkValue, transactionManager)
                .reader(multiApplicationResourceItemReader)
                .processor(validateApplicationProcessor)
                .writer(applicationClassifierCompositeItemWriter)
                .listener((ItemWriteListener<? super Application>) flagApplicationDataWriterListener)
                .faultTolerant().skip(DataIntegrityViolationException.class).skipPolicy(new AlwaysSkipItemSkipPolicy())
                .stream(unvalidatedApplicationItemWriter)
                .build();
    }

    /**
     * Multi Application resource reader configuration.
     *
     * @param sessionPath                      the session date formatted (subfolder).
     * @param readApplicationWorkingFileReader the delegate reader.
     * @param loadingFileStorage               to access resource file.
     * @return the configured multi resource reader.
     */
    @Bean
    @StepScope
    public MultiResourceItemReader<Application> multiApplicationResourceItemReader(@Value("#{jobExecutionContext['session.path']}") final String sessionPath, final FlatFileItemReader<Application> readApplicationWorkingFileReader, final FileStorage loadingFileStorage) throws IOException {
        return new MultiResourceItemReaderBuilder<Application>()
                .name("multiApplicationResourceItemReader")
                .resources(loadingFileStorage.listResources(FileFolder.WORK, sessionPath, FileType.APPLICATION))
                .delegate(readApplicationWorkingFileReader)
                .build();
    }

    /**
     * Application FlatFileItemReader definition.
     *
     * @param fileInfo to access input file information.
     * @return the configured Reader.
     */
    @Bean
    @StepScope
    public FlatFileItemReader<Application> readApplicationWorkingFileReader(final FileMapperInfo fileInfo) {
        final CsvLineMapper<Application> mapper = new CsvLineMapper<>();
        final DelimitedLineTokenizer tokenizer = new QuotelessLineTokenizer();
        tokenizer.setDelimiter(";");
        mapper.setLineTokenizer(tokenizer);
        final BeanWrapperFieldSetMapper<Application> beanWrapper = new BeanWrapperFieldSetMapper<>();
        beanWrapper.setTargetType(Application.class);
        mapper.setFieldSetMapper(beanWrapper);
        return new FlatFileItemReaderBuilder<Application>().name("readApplicationWorkingFileReader")
                .lineMapper(mapper)
                .strict(false)
                .linesToSkip(1)
                .skippedLinesCallback(line -> skippedLinesCallback(FileType.APPLICATION, line, tokenizer, fileInfo))
                .build();
    }

    /**
     * Application processor definition.
     *
     * @param virtualEquipmentRepository repository to access VirtualEquipment data.
     * @param inventoryId                the inventory unique identifier.
     * @param sessionDate                the session date.
     * @param validatorFactory           the created validator
     * @return the configured processor.
     */
    @Bean
    @StepScope
    public ApplicationItemProcessor validateApplicationProcessor(
            final VirtualEquipmentRepository virtualEquipmentRepository,
            @Value(("#{jobParameters['inventory.id']}")) final long inventoryId,
            @Value("#{jobParameters['session.date']}") final Date sessionDate,
            @Value("#{jobParameters['locale']}") final Locale locale,
            final Validator validatorFactory) {
        return new ApplicationItemProcessor(virtualEquipmentRepository, sessionDate, inventoryId, validatorFactory);
    }

    /**
     * Classifier composite item writer configuration to write the valid data to the database, and the bad data to a reject file.
     *
     * @param flagApplicationWriterClassifier the custom classifier based on 'valid' attribut.
     * @return the configured writer.
     */
    @Bean
    public ClassifierCompositeItemWriter<Application> applicationClassifierCompositeItemWriter(final FlaggedDataWriterClassifier<Application> flagApplicationWriterClassifier) {
        return new ClassifierCompositeItemWriterBuilder<Application>().classifier(flagApplicationWriterClassifier).build();
    }

    /**
     * Classifier configuration, based on 'valid' attribut.
     *
     * @param persistApplicationWriter         the spring writer to database.
     * @param unvalidatedApplicationItemWriter the spring writer to reject file.
     * @return the configured classifier.
     */
    @Bean
    public FlaggedDataWriterClassifier<Application> flagApplicationWriterClassifier(
            final RepositoryItemWriter<Application> persistApplicationWriter, final FlatFileItemWriter<Application> unvalidatedApplicationItemWriter) {
        return new FlaggedDataWriterClassifier<>(persistApplicationWriter, unvalidatedApplicationItemWriter);
    }

    /**
     * Application RepositoryItemWriter definition to write good data in database.
     *
     * @param applicationRepository repository to write data in database.
     * @return the configured writer.
     */
    @Bean
    public RepositoryItemWriter<Application> persistApplicationWriter(final ApplicationRepository applicationRepository) {
        final RepositoryItemWriterBuilder<Application> builder = new RepositoryItemWriterBuilder<>();
        return builder.repository(applicationRepository).methodName("save").build();
    }

    /**
     * Unvalidated Application FlatFileItemWriter definition to write bad data in reject file.
     *
     * @param sessionPath        the formatted session date.
     * @param localWorkingFolder generated local working folder
     * @param fileInfo           to get file's information tool.
     * @return the configured writer.
     */
    @Bean
    @StepScope
    public FlatFileItemWriter<Application> unvalidatedApplicationItemWriter(
            @Value("#{jobExecutionContext['session.path']}") final String sessionPath,
            @Value("#{jobParameters['local.working.folder']}") final String localWorkingFolder,
            final FileMapperInfo fileInfo) {
        final List<String> headers = new ArrayList<>(List.copyOf(fileInfo.getMapping(FileType.APPLICATION).stream().map(Header::getName).toList()));
        headers.add("nomEquipementPhysique");
        headers.add("inputFileName");
        headers.add("lineNumber");
        headers.add("message");
        return new FlatFileItemWriterBuilder<Application>()
                .name("unvalidatedApplicationItemWriter")
                .resource(new FileSystemResource(Path.of(localWorkingFolder, String.join("", "rejected_application_", sessionPath, ".csv"))))
                .delimited().delimiter(";").names(headers.toArray(String[]::new))
                .headerCallback(writer -> writer.write(String.join(";", headers)))
                .build();
    }

    /**
     * Item Writer listener to process duplicates.
     *
     * @param applicationRepository the repository to update data.
     * @return the configured listener.
     */
    @Bean
    public FlagApplicationDataWriterListener flagApplicationDataWriterListener(final ApplicationRepository applicationRepository) {
        return new FlagApplicationDataWriterListener(applicationRepository, ApplicationMapper.INSTANCE);
    }
}
