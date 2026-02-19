package com.elocate.elocate.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for admin to create partner account directly
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminCreatePartnerRequest {
    
    // User credentials
    @NotBlank(message = "Full name is required")
    private String fullName;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    @NotBlank(message = "Temporary password is required")
    private String temporaryPassword;
    
    @NotBlank(message = "Mobile number is required")
    private String mobileNumber;
    
    // Facility details
    @NotBlank(message = "Registration number is required")
    private String registrationNumber;
    
    @NotBlank(message = "Facility name is required")
    private String facilityName;
    
    @NotBlank(message = "Address is required")
    private String address;
    
    @NotNull(message = "Latitude is required")
    private Double latitude;
    
    @NotNull(message = "Longitude is required")
    private Double longitude;
    
    private Integer capacity;
    
    private String contactNumber;
    
    private String operatingHours;
    
    private String state;
    
    private String pincode;
    
    @Builder.Default
    private Boolean autoApprove = true; // Admin-created partners are auto-approved
}
