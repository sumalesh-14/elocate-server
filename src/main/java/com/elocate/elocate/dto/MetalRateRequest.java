package com.elocate.elocate.dto;

import com.elocate.elocate.model.enums.MetalType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetalRateRequest {
    
    @NotNull(message = "Metal type is required")
    private MetalType metalType;
    
    @NotNull(message = "Unit is required")
    private String unit;
    
    @NotNull(message = "Points per unit is required")
    @Positive(message = "Points per unit must be positive")
    private BigDecimal pointsPerUnit;
    
    @NotNull(message = "Effective from date is required")
    private LocalDate effectiveFrom;
    
    private LocalDate effectiveTo;
    
    @Builder.Default
    private Boolean isActive = true;
}
