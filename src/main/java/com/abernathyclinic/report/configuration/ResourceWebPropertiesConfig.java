package com.abernathyclinic.report.configuration;


import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ResourceWebPropertiesConfig {
	@Bean
    WebProperties.Resources resources() {
        return new WebProperties.Resources();
    }
}
