package com.elocate.elocate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyRecycleRequestDto {
    
    @NotNull(message = "Verified weight is required")
    @Positive(message = "Weight must be positive")
    private BigDecimal verifiedWeightGrams;
    
    @NotBlank(message = "Verified condition code is required")
    private String verifiedConditionCode;
    
    private String verificationNotes;
}
