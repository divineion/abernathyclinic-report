package com.abernathyclinic.report.configuration;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import org.springframework.web.reactive.function.client.WebClient;


@Configuration
public class WebClientConfiguration {
	
	@Bean
	WebClient webClient() {
		return WebClient
				.builder()
				.baseUrl("localhost:8080")
				.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.build();
	}
}
