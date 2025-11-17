package com.abernathyclinic.report.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;

import reactor.core.publisher.Flux;

@Service
public class KeyWordsReader {

    private final ObjectMapper mapper;

    public KeyWordsReader(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    private final File dataFilePath = new File("src/main/resources/data/risk-keywords.json");

    private Flux<String> keyWords;

    private static class KeywordsData {
        public Set<String> keywords;
    }

    @PostConstruct
    public void readData() {
        try (InputStream inputStream = new FileInputStream(dataFilePath)) {
            KeywordsData data = mapper.readValue(inputStream, KeywordsData.class);
            keyWords = Flux.fromIterable(data.keywords); 
        } catch (Exception e) {
            throw new RuntimeException("Failed to read keywords JSON file", e);
        }
    }

    public Flux<String> keyWords() {
        return keyWords;
    }
}
