package com.elocate.elocate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Structured user profile response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {

    private String status;
    private String message;
    private UserData user;
    private TokenData tokens;
    private AddressData address;
    private WalletData wallet;
    private FacilityData facility;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FacilityData {
        private UUID id;
        private String facilityName;
        private String operatingHours;
        private String address;
        private String city;
        private String state;
        private String pincode;
        private java.math.BigDecimal latitude;
        private java.math.BigDecimal longitude;
        private String registrationNumber;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserData {
        private UUID id;
        private String fullName;
        private String email;
        private String mobileNumber;
        private String role;
        private UUID facilityId; // populated for INTERMEDIARY/PARTNER roles
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressData {
        private UUID id;
        private String address;
        private String city;
        private String state;
        private String pincode;
        private java.math.BigDecimal latitude;
        private java.math.BigDecimal longitude;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WalletData {
        private java.math.BigDecimal pointsBalance;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TokenData {
        private String accessToken; // Local JWT for API access
        private String refreshToken; // Refresh token
        private String providerAccessToken; // Provider (Auth0) token
        private long expiresIn; // Expiration time in milliseconds
    }
}
