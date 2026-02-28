package com.elocate.elocate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;

import java.sql.ResultSet;
import java.sql.Statement;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DbStartupCheck {

    private final DataSource dataSource;

    @Bean
    public ApplicationRunner checkDbConnection() {
        return args -> {
            log.info("🔍 Checking database connection on startup...");

            try (Connection connection = dataSource.getConnection()) {
                boolean isValid = connection.isValid(2);

                if (isValid) {
                    log.info("✅ Database connection initialized successfully");

                    // Try to perform a select count from the user table
                    try (Statement statement = connection.createStatement()) {
                        ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM \"user\"");
                        if (resultSet.next()) {
                            long count = resultSet.getLong(1);
                            log.info("📊 Database check successful! Found {} users and connection is working.", count);
                        }
                    } catch (Exception e) {
                        log.warn(
                                "⚠️ Connection is OK, but table check failed: {}. Database might be empty or table might not exist yet.",
                                e.getMessage());
                    }
                } else {
                    throw new IllegalStateException("❌ Database connection is not valid");
                }
            } catch (Exception e) {
                log.error("❌ Failed to connect to database: {}", e.getMessage());
            }
        };
    }
}
