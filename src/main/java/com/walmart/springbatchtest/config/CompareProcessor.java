package com.walmart.springbatchtest.config;

import org.springframework.batch.item.ItemProcessor;

public class CompareProcessor implements ItemProcessor<String, Boolean> {

    public CompareProcessor(String compareString1, String compareString2) {
    }

    @Override
    public Boolean process(String s) throws Exception {
        return null;
    }
}
