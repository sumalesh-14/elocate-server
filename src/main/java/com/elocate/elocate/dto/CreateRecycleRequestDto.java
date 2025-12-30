package com.elocate.elocate.dto;

import com.elocate.elocate.model.enums.FulfillmentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRecycleRequestDto {
    
    @NotNull(message = "Device model ID is required")
    private UUID deviceModelId;
    
    @NotBlank(message = "Condition code is required")
    private String conditionCode;
    
    @NotNull(message = "Fulfillment type is required")
    private FulfillmentType fulfillmentType;
    
    /**
     * Optional - if not provided for PICKUP, default address will be used
     */
    private UUID pickupAddressId;
    
    /**
     * Required for DROP_OFF fulfillment type
     */
    private UUID facilityId;
    
    private String notes;
}

