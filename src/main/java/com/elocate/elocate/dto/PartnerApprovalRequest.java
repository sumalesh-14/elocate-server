package com.elocate.elocate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for approving/rejecting partner registration
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerApprovalRequest {
    
    @NotBlank(message = "Approval status is required")
    private String approvalStatus; // APPROVED, REJECTED
    
    private String remarks;
    
    @Builder.Default
    @NotNull(message = "Verification status is required")
    private Boolean isVerified = true; // Auto-verify when approved
}
