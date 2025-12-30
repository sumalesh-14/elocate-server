package com.elocate.elocate.dto;

import com.elocate.elocate.model.enums.FulfillmentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO for updating fulfillment status of a recycle request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFulfillmentStatusDto {
    
    @NotNull(message = "New status is required")
    private FulfillmentStatus newStatus;
    
    /**
     * Optional notes about the status change
     */
    private String notes;
    
    /**
     * User ID who is making the change (admin, agent, or system)
     */
    private UUID changedBy;
}
