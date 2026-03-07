package com.elocate.elocate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for admin to reassign recycle request to different facility
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacilityReassignmentRequest {
    
    @NotNull(message = "New facility ID is required")
    private UUID newFacilityId;
    
    @NotBlank(message = "Reason for reassignment is required")
    private String reason;
}
