package com.elocate.elocate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for intermediary to approve recycle request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntermediaryApprovalRequest {
    private BigDecimal adjustedEstimatedPoints;
    private String adjustmentReason;
}
