package com.walmart.springbatchtest.config;

import com.ibm.mq.jms.MQQueue;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.jms.JmsItemReader;
import org.springframework.batch.item.jms.JmsItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.util.Assert;

import javax.jms.JMSException;
import java.util.List;

@Configuration
@EnableBatchProcessing
public class InputReader {
    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private JmsTemplate jmsTemplate;

    public List<? extends String> compareString1;

    public List<? extends String> compareString2;


    @Bean
    public FlatFileItemReader<String> reader(){
        FlatFileItemReader<String> itemReader = new FlatFileItemReader<>();
        itemReader.setResource(new ClassPathResource("inputFiles/I0001.IN.xml"));
        itemReader.setName("csvReader");
        itemReader.setLineMapper(new LineMapper<String>() {
            @Override
            public String mapLine(String s, int i) throws Exception {
                return s;
            }
        });
        itemReader.setLinesToSkip(1);
        return itemReader;
    }

    @Bean
    public FlatFileItemReader<String> outputReader() throws Exception {
        FlatFileItemReader<String> itemReader = new FlatFileItemReader<>();
        itemReader.setResource(new ClassPathResource("outputFiles/I0001.OUT.xml"));
        itemReader.setName("outputReader");
        itemReader.setLineMapper(new LineMapper<String>() {
            @Override
            public String mapLine(String s, int i) throws Exception {
                return s;
            }
        });
        itemReader.open(new ExecutionContext());

        System.out.println(compareString2 + "compareString2");
        return itemReader;
    }

    @Bean
    public  InputProcessor processor(){
        return new InputProcessor(

        );
    }

//    @Bean
//    public CompareProcessor compareProcessor(){
//
//    }

    public CompareProcessor compareProcessor(String compareString1, String compareString2){
        return new CompareProcessor(compareString1, compareString2);
    }

    @Bean
    public JmsItemWriter<String> writer() throws JMSException {
        JmsItemWriter<String> itemWriter = new JmsItemWriter<>();
        MQQueue destination = new MQQueue("ENTRY");
        jmsTemplate.setDefaultDestination(destination);
        itemWriter.setJmsTemplate(jmsTemplate);
        return itemWriter;
    }

    @Bean
    public JmsItemReader<String> jmsItemReader() throws JMSException {
        JmsItemReader<String> itemReader = new JmsItemReader<>();
        MQQueue destination = new MQQueue("ENTRY");
        long timeout = 5;
        jmsTemplate.setDefaultDestination(destination);
        jmsTemplate.setReceiveTimeout(timeout);
        itemReader.setJmsTemplate(jmsTemplate);

        System.out.println(compareString1 + "compareString1");
        return itemReader;
    }

    @Bean
    public Step step1() throws JMSException {
        return stepBuilderFactory.get("input-step").<String, String>chunk(1)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .build();
    }

    @Bean
    public Step step2() throws JMSException {
        return stepBuilderFactory.get("jms-reader").<String, String>chunk(1)
                .reader(jmsItemReader())
                .processor(processor())
                .writer(new ItemWriter<String>() {
                    @Override
                    public void write(List<? extends String> list) throws Exception {
                        compareString1 = list;
                        System.out.println(compareString1);
                    }
                }).build();
    }

    @Bean
    public Step step3() throws Exception {
        return stepBuilderFactory.get("output-reader").<String, String>chunk(1)
                .reader(outputReader())
                .processor(processor())
                .writer(new ItemWriter<String>() {
                    @Override
                    public void write(List<? extends String> list) throws Exception {
                        compareString2 = list;
                        System.out.println(compareString2);
                    }
                })
                .build();
    }

    @Bean
    public Job job() throws Exception {
        return jobBuilderFactory.get("inputStep")
                .flow(step1())
                .next(step2())
                .next(step3())
                .end().build();
    }

    //@Bean
    public void compareString(String compareString1, String compareString2){
        System.out.println(compareString1 + "");
        Boolean comparison = compareString1.equals(compareString2);
        Assert.isTrue(comparison, "Messages are equal");
    }
}
