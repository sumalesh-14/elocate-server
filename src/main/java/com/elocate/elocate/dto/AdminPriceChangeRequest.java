package com.elocate.elocate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for admin to change pricing on recycle requests
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminPriceChangeRequest {
    
    @Positive(message = "Estimated points must be positive")
    private BigDecimal estimatedAmount;
    
    @Positive(message = "Final points must be positive")
    private BigDecimal finalAmount;
    
    @NotBlank(message = "Reason for price change is required")
    private String reason;
}
