package com.elocate.elocate.service;

import com.elocate.elocate.dto.CertificateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Service for generating recycling certificates via Python Image Generation API
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ImageGenerationService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${image.generation.api.url:}")
    private String imageGenerationApiUrl;

    @Value("${image.generation.api.enabled:false}")
    private boolean imageGenerationEnabled;

    /**
     * Generate recycling certificate
     * 
     * @param request Certificate request with citizen and device details
     * @return Optional containing certificate image URL, empty if generation fails
     */
    public Optional<byte[]> generateCertificate(CertificateRequest request) {
        if (!imageGenerationEnabled || imageGenerationApiUrl == null || imageGenerationApiUrl.isBlank()) {
            log.warn("Image Generation API is disabled or not configured");
            return Optional.empty();
        }

        log.info("Generating certificate for request: {}", request.getRequestId());

        try {
            // Prepare request headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Create request entity
            HttpEntity<CertificateRequest> requestEntity = new HttpEntity<>(request, headers);

            // Make API call
            ResponseEntity<byte[]> response = restTemplate.exchange(
                imageGenerationApiUrl + "/certificate/generate",
                HttpMethod.POST,
                requestEntity,
                byte[].class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.info("Certificate generated successfully for request: {}", request.getRequestId());
                return Optional.of(response.getBody());
            } else {
                log.warn("Certificate generation returned non-OK status: {}", response.getStatusCode());
                return Optional.empty();
            }

        } catch (Exception e) {
            log.error("Error generating certificate for request: {}", request.getRequestId(), e);
            return Optional.empty();
        }
    }

    /**
     * Check if Image Generation API is available
     */
    public boolean isImageGenerationAvailable() {
        return imageGenerationEnabled && imageGenerationApiUrl != null && !imageGenerationApiUrl.isBlank();
    }
}
