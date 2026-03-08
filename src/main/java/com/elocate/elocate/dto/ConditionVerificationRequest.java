package com.elocate.elocate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for intermediary to verify device condition
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConditionVerificationRequest {
    private String verifiedConditionCode;
    private String notes; // Comments for history
    private BigDecimal finalAmount; // Final amount to be paid
}
