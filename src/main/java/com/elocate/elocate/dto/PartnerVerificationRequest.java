package com.elocate.elocate.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerVerificationRequest {
    
    @NotNull(message = "Verification status is required")
    private Boolean isVerified;
    
    private String remarks;
}
