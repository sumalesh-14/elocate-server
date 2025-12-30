package com.elocate.elocate.dto;

import com.elocate.elocate.model.enums.FulfillmentStatus;
import com.elocate.elocate.model.enums.FulfillmentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecycleRequestResponse {
    
    private UUID id;
    private UUID deviceModelId;
    private String deviceModelName;
    private String brandName;
    private String categoryName;
    private String conditionCode;
    private BigDecimal estimatedPoints;
    private BigDecimal finalPoints;
    private String status;
    
    // Fulfillment tracking fields
    private FulfillmentType fulfillmentType;
    private FulfillmentStatus fulfillmentStatus;
    private String fulfillmentStatusDisplay;
    private UUID pickupAddressId;
    private UUID facilityId;
    private String facilityName;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

