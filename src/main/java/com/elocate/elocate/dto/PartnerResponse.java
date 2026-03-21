package com.elocate.elocate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerResponse {
    private UUID id;
    private String registrationNumber;
    private String name;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer capacity;
    private String operatingHours;
    private String state;
    private String pincode;
    private Boolean isVerified;
    private Boolean isActive;
    private String fullName;        // from user.fullName
    private String email;           // from user.email
    private String mobileNumber;    // from user.mobileNumber
    private String approvalStatus;
    private String documentUrl;          // kept for backward compat
    private List<String> documentUrls;   // list of up to 3 URLs
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
