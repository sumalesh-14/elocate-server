package com.elocate.elocate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Complete user profile response including user, address, and wallet info
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    
    private UUID id;
    private String fullName;
    private String mobileNumber;
    private String email;
    
    // Firebase authentication token (for API requests)
    private String firebaseToken;
    
    // JWT token (for API authentication)
    private String jwtToken;
    
    // Address Info
    private AddressInfo address;
    
    // Wallet Info
    private WalletInfo wallet;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressInfo {
        private UUID id;
        private String address;
        private String city;
        private String state;
        private String pincode;
        private BigDecimal latitude;
        private BigDecimal longitude;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WalletInfo {
        private BigDecimal pointsBalance;
    }
}
