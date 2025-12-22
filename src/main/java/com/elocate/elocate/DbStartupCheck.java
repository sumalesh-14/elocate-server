package com.elocate.elocate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DbStartupCheck {

    private final DataSource dataSource;

    @Bean
    public ApplicationRunner checkDbConnection(){
        return args -> {
            log.info("🔍 Checking database connection on startup...");

            try (Connection connection = dataSource.getConnection()) {
                boolean isValid = connection.isValid(2);

                if (isValid) {
                    log.info("✅ Database connection initialized successfully");
                } else {
                    throw new IllegalStateException("❌ Database connection is not valid");
                }
            }
        };
    }
}
