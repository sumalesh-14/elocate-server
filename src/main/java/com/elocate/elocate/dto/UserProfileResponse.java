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
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TokenData {
        private String accessToken;       // Local JWT for API access
        private String refreshToken;      // Refresh token
        private String providerAccessToken; // Provider (Auth0) token
        private long expiresIn;           // Expiration time in milliseconds
    }
}
