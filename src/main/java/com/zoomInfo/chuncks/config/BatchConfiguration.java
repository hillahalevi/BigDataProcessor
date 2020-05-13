package com.zoomInfo.chuncks.config;

import com.zoomInfo.model.Entry;
import com.zoomInfo.model.mapper.EntryFieldSetMapper;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.MapJobRepositoryFactoryBean;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import static com.zoomInfo.Constants.*;

/**
 * using spring batch job mechanism plus thread pool for parallel  processing = better performance
 * results : 1000000 entries processed in 2s413ms  (chunk size -1000, pool limit = 20)
 * results : 100 entries processed in 52ms  (chunk size -20, pool limit = 10)
 */

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {


    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;


    @Bean
    public LineMapper<Entry> lineMapper() {
        final DefaultLineMapper<Entry> defaultLineMapper = new DefaultLineMapper<>();
        final DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter(",");
        lineTokenizer.setStrict(false);
        lineTokenizer.setNames(ENTRY_PROPERTIES_COLUMNS);
        final EntryFieldSetMapper fieldSetMapper = new EntryFieldSetMapper();
        defaultLineMapper.setLineTokenizer(lineTokenizer);
        defaultLineMapper.setFieldSetMapper(fieldSetMapper);
        return defaultLineMapper;
    }

    @Bean
    public FlatFileItemReader<Entry> reader() {
        return new FlatFileItemReaderBuilder<Entry>()
                .name("entryItemReader")
                .resource(new FileSystemResource(TEST_FILE))
                .delimited()
                .names(ENTRY_PROPERTIES_COLUMNS)
                .saveState(false) //thread safe
                .lineMapper(lineMapper())
                .fieldSetMapper(new BeanWrapperFieldSetMapper<Entry>() {{
                    setTargetType(Entry.class);
                }})
                .build();
    }

    @Bean
    public ItemProcessor<Entry, String> processor() {
        return entry -> {
            if (entry.getFirstName().contains("'")) {
                return entry.getId();
            }
            return null;
        };
    }

    @Bean
    public SynchronizedWrapperWriter<String> writer() {
        //Create writer instance
        FlatFileItemWriter<String> writer = new FlatFileItemWriter<>();
        Resource outputResource = new FileSystemResource(OUTPUT_DATA_CSV);
        //Set output file location
        writer.setResource(outputResource);

        //All job repetitions should "append" to same output file
        writer.setShouldDeleteIfExists(true);
        writer.setAppendAllowed(true);
        writer.setLineAggregator(s -> s);
        writer.setSaveState(false);
        return new SynchronizedWrapperWriter<>(writer);
    }


    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1").<Entry, String>chunk(CHUNK_SIZE)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .taskExecutor(taskExecutor()).throttleLimit(THROTTLE_LIMIT)
                .build();
    }

    @Bean
    public JobRepository jobRepository() throws Exception {
        MapJobRepositoryFactoryBean factory = new MapJobRepositoryFactoryBean();
        factory.setTransactionManager(transactionManager());
        return factory.getObject();
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        return new ResourcelessTransactionManager();
    }

    @Bean
    public Job batchJob() {
        return jobBuilderFactory
                .get("batchJob")
                .incrementer(new RunIdIncrementer())
                .start(step1())
                .build();
    }

    @Bean
    public JobLauncher jobLauncher() throws Exception {
        SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        jobLauncher.setJobRepository(jobRepository());
        return jobLauncher;
    }

    @Bean
    public TaskExecutor taskExecutor() {
        return new SimpleAsyncTaskExecutor("spring_batch");
    }


}