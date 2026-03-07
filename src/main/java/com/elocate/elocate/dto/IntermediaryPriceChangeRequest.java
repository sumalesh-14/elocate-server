package com.elocate.elocate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for intermediary to change pricing after product receipt
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntermediaryPriceChangeRequest {
    
    @NotNull(message = "New final points is required")
    @Positive(message = "Final points must be positive")
    private BigDecimal newFinalPoints;
    
    @NotBlank(message = "Reason for price change is required")
    private String reason;
}
