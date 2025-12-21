package com.elocate.elocate.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.datasource")
public class DatabaseConfig {
    // Spring Boot will automatically configure DataSource
    // This class can be extended for custom database configurations if needed
}