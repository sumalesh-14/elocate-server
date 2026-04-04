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

                    try (Statement statement = connection.createStatement()) {
                        // User count check
                        ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM \"user\"");
                        if (resultSet.next()) {
                            long count = resultSet.getLong(1);
                            log.info("📊 Database check successful! Found {} users and connection is working.", count);
                        }
                    } catch (Exception e) {
                        log.warn("⚠️ Connection is OK, but table check failed: {}. Database might be empty or table might not exist yet.", e.getMessage());
                    }

                    // Check withdrawal_request table exists — auto-create if missing
                    try (Statement statement = connection.createStatement()) {
                        ResultSet rs = statement.executeQuery(
                            "SELECT COUNT(*) FROM information_schema.tables " +
                            "WHERE table_schema = 'public' AND table_name = 'withdrawal_request'"
                        );
                        if (rs.next() && rs.getLong(1) == 0) {
                            log.warn("⚠️ withdrawal_request table not found — creating it now...");
                            statement.execute(
                                "CREATE TABLE IF NOT EXISTS public.withdrawal_request (" +
                                "  id UUID PRIMARY KEY DEFAULT gen_random_uuid()," +
                                "  user_id UUID NOT NULL," +
                                "  amount NUMERIC(10,2) NOT NULL," +
                                "  account_holder_name VARCHAR(255) NOT NULL," +
                                "  mobile_number VARCHAR(15) NOT NULL," +
                                "  account_number VARCHAR(18) NOT NULL," +
                                "  bank_name VARCHAR(255) NOT NULL," +
                                "  ifsc_code VARCHAR(11) NOT NULL," +
                                "  upi_id VARCHAR(255)," +
                                "  email VARCHAR(255)," +
                                "  status VARCHAR(20) NOT NULL DEFAULT 'PENDING'," +
                                "  admin_note TEXT," +
                                "  processed_by UUID," +
                                "  requested_at TIMESTAMP NOT NULL DEFAULT now()," +
                                "  processed_at TIMESTAMP" +
                                ")"
                            );
                            log.info("✅ withdrawal_request table created successfully.");
                        } else {
                            log.info("✅ withdrawal_request table exists.");
                        }
                    } catch (Exception e) {
                        log.error("❌ Failed to verify/create withdrawal_request table: {}", e.getMessage());
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
