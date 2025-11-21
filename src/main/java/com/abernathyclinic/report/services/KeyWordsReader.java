package com.abernathyclinic.report.services;

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

    private Flux<String> keyWords;

    private static class KeywordsData {
        public Set<String> keywords;
    }

    @PostConstruct
    public void readData() {
    	// https://www.baeldung.com/java-getresourceasstream-vs-fileinputstream
    	// This method is commonly used to read configuration files, properties files, and other resources packaged with the application.
    	// fonctionne en dev comme dans un JAR
        try (InputStream inputStream = getClass().getResourceAsStream("/data/risk-keywords.json")) {
        	
        	if (inputStream == null) {
                throw new RuntimeException("File not found in classpath: /data/risk-keywords.json");
            }
        	
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
