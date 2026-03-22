package com.elocate.elocate.dto;

import com.elocate.elocate.model.enums.FulfillmentStatus;
import com.elocate.elocate.model.enums.FulfillmentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecycleRequestResponse {

    private UUID id;
    private String requestNumber; // Human-readable request ID (e.g., RCY-2024-000001)
    private UUID userId; // The citizen's ID
    private String citizenName;
    private String citizenEmail;
    private UUID deviceModelId;
    private String deviceModelName;
    private String brandName;
    private String categoryName;
    private String conditionCode;
    private BigDecimal estimatedAmount;
    private BigDecimal finalAmount;
    private String status;

    // Fulfillment tracking fields
    private FulfillmentType fulfillmentType;
    private FulfillmentStatus fulfillmentStatus;
    private String fulfillmentStatusDisplay;
    private UUID pickupAddressId;
    private UUID facilityId;
    private String facilityName;
    private String facilityAddress;
    private String facilityEmail;
    private String facilityPhone;

    // Pickup address details (only present for PICKUP type)
    private String pickupAddress;
    private String pickupCity;
    private String pickupState;
    private String pickupPincode;
    private UUID assignedDriverId;
    private String driverName;
    private String driverFailureReason;

    // Scheduled pickup date
    private LocalDate pickupDate;

    // Certificate URL (available after recycling is complete)
    private String certificateUrl;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
