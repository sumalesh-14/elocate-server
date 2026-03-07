package com.elocate.elocate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Request DTO for certificate generation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateRequest {
    
    @JsonProperty("citizen_name")
    private String citizenName;
    
    @JsonProperty("device_name")
    private String deviceName;
    
    @JsonProperty("recycling_date")
    private String recyclingDate;
    
    @JsonProperty("request_id")
    private String requestId;
    
    @JsonProperty("environmental_impact")
    private Map<String, Object> environmentalImpact;
}
