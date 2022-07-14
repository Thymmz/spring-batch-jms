package com.walmart.springbatchtest.config;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.jms.core.MessageCreator;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

public class InputProcessor implements ItemProcessor<String, String> {
    @Override
    public String process(String s) throws Exception {
        //System.out.println(s);
        return s;
    }
}
