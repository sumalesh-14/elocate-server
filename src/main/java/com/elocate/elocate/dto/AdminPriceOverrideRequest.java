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
 * Request DTO for admin to override device model pricing
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminPriceOverrideRequest {
    
    @NotNull(message = "Override price is required")
    @Positive(message = "Override price must be positive")
    private BigDecimal overridePrice;
    
    @NotBlank(message = "Reason for price override is required")
    private String reason;
}
