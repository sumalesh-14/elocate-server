package com.elocate.elocate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for AI Pricing API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIPricingRequest {
    private String deviceModelName;
    private String categoryName;
    private String brandName;
    private String conditionCode;
}
