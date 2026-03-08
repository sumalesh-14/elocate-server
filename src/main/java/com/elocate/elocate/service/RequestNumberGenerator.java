package com.elocate.elocate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;

/**
 * Service to generate human-readable request numbers
 * Format: RCY-YYYY-NNNNNN (e.g., RCY-2024-000001)
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RequestNumberGenerator {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Generate next request number in format: RCY-YYYY-NNNNNN
     * 
     * @return Formatted request number
     */
    @Transactional
    public String generateRequestNumber() {
        // Get current year
        int currentYear = Year.now().getValue();
        
        // Get next sequence value
        Long sequenceValue = jdbcTemplate.queryForObject(
            "SELECT nextval('recycle_request_seq')", 
            Long.class
        );
        
        if (sequenceValue == null) {
            sequenceValue = 1L;
        }
        
        // Format: RCY-YYYY-NNNNNN (6 digits with leading zeros)
        String requestNumber = String.format("RCY-%d-%06d", currentYear, sequenceValue);
        
        log.debug("Generated request number: {}", requestNumber);
        return requestNumber;
    }

    /**
     * Check if request number already exists
     * 
     * @param requestNumber Request number to check
     * @return true if exists, false otherwise
     */
    public boolean exists(String requestNumber) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM recycle_request WHERE request_number = ?",
            Integer.class,
            requestNumber
        );
        return count != null && count > 0;
    }

    /**
     * Generate unique request number (with retry logic in case of collision)
     * 
     * @return Unique request number
     */
    @Transactional
    public String generateUniqueRequestNumber() {
        int maxRetries = 5;
        int attempt = 0;
        
        while (attempt < maxRetries) {
            String requestNumber = generateRequestNumber();
            
            if (!exists(requestNumber)) {
                return requestNumber;
            }
            
            attempt++;
            log.warn("Request number collision detected: {}. Retry attempt: {}", requestNumber, attempt);
        }
        
        throw new RuntimeException("Failed to generate unique request number after " + maxRetries + " attempts");
    }
}
