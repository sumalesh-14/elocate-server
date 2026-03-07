package com.elocate.elocate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Request DTO for intermediary to approve recycle request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntermediaryApprovalRequest {
    private BigDecimal adjustedEstimatedAmount;
    private String adjustmentReason;
    private Map<String, Object> aiPricingResponse;
}
