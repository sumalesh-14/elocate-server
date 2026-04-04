package com.elocate.elocate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for partner dashboard data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerDashboardResponse {
    
    // Facility info
    private UUID facilityId;
    private String registrationNumber;
    private String facilityName;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer capacity;
    private String operatingHours;
    private String state;
    private String pincode;
    private String approvalStatus;
    private Boolean isVerified;
    private Boolean isActive;
    private LocalDateTime createdAt;
    
    // User info
    private UUID userId;
    private String fullName;
    private String mobileNumber;
    
    // Statistics
    private Long totalRequests;
    private Long pendingRequests;
    private Long completedRequests;
    private Long rejectedRequests;
    private BigDecimal totalCredited; // sum of RECYCLED wallet transactions for this facility
}
