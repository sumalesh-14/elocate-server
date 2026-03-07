package com.elocate.elocate.service;

import com.elocate.elocate.dto.AIPricingRequest;
import com.elocate.elocate.dto.AIPricingResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Optional;

/**
 * Service for integrating with external AI Pricing API
 * Provides pricing estimates based on device characteristics
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AIPricingService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${ai.pricing.api.url:}")
    private String aiPricingApiUrl;

    @Value("${ai.pricing.api.key:}")
    private String aiPricingApiKey;

    @Value("${ai.pricing.api.timeout:3000}")
    private int timeoutMs;

    @Value("${ai.pricing.api.enabled:false}")
    private boolean aiPricingEnabled;

    /**
     * Get AI pricing estimate for a device
     * Results are cached for 24 hours to reduce API calls
     * 
     * @param request Pricing request with device details
     * @return Optional containing pricing response, empty if API fails
     */
    @Cacheable(value = "aiPricing", key = "#request.deviceModelName + '_' + #request.conditionCode", 
               unless = "#result == null || #result.isEmpty()")
    public Optional<AIPricingResponse> getAIPricing(AIPricingRequest request) {
        if (!aiPricingEnabled || aiPricingApiUrl == null || aiPricingApiUrl.isBlank()) {
            log.warn("AI Pricing API is disabled or not configured");
            return Optional.empty();
        }

        log.info("Calling AI Pricing API for device: {}, category: {}, brand: {}", 
                 request.getDeviceModelName(), request.getCategoryName(), request.getBrandName());

        try {
            // Prepare request headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (aiPricingApiKey != null && !aiPricingApiKey.isBlank()) {
                headers.set("Authorization", "Bearer " + aiPricingApiKey);
            }

            // Create request entity
            HttpEntity<AIPricingRequest> requestEntity = new HttpEntity<>(request, headers);

            // Make API call with timeout
            ResponseEntity<String> response = restTemplate.exchange(
                aiPricingApiUrl + "/pricing/estimate",
                HttpMethod.POST,
                requestEntity,
                String.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                AIPricingResponse pricingResponse = objectMapper.readValue(
                    response.getBody(), 
                    AIPricingResponse.class
                );
                
                log.info("AI Pricing API returned estimate: {} with {} materials", 
                         pricingResponse.getEstimatedPrice(), 
                         pricingResponse.getPreciousMaterials() != null ? 
                         pricingResponse.getPreciousMaterials().size() : 0);
                
                return Optional.of(pricingResponse);
            } else {
                log.warn("AI Pricing API returned non-OK status: {}", response.getStatusCode());
                return Optional.empty();
            }

        } catch (ResourceAccessException e) {
            // Timeout or connection error
            log.error("AI Pricing API timeout or connection error: {}", e.getMessage());
            return Optional.empty();
            
        } catch (HttpClientErrorException e) {
            // 4xx errors (authentication, bad request, etc.)
            log.error("AI Pricing API client error: {} - {}", e.getStatusCode(), e.getMessage());
            return Optional.empty();
            
        } catch (HttpServerErrorException e) {
            // 5xx errors (server error)
            log.error("AI Pricing API server error: {} - {}", e.getStatusCode(), e.getMessage());
            return Optional.empty();
            
        } catch (Exception e) {
            // Any other error (parsing, etc.)
            log.error("Unexpected error calling AI Pricing API", e);
            return Optional.empty();
        }
    }

    /**
     * Check if AI Pricing API is available and configured
     */
    public boolean isAIPricingAvailable() {
        return aiPricingEnabled && aiPricingApiUrl != null && !aiPricingApiUrl.isBlank();
    }
}
