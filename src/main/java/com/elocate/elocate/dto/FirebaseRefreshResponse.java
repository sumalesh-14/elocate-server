package com.elocate.elocate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FirebaseRefreshResponse {
    
    @JsonProperty("access_token")
    private String accessToken; // This is the new ID Token
    
    @JsonProperty("expires_in")
    private String expiresIn;
    
    @JsonProperty("token_type")
    private String tokenType;
    
    @JsonProperty("refresh_token")
    private String refreshToken; // New refresh token
    
    @JsonProperty("id_token")
    private String idToken;
    
    @JsonProperty("user_id")
    private String userId;
    
    @JsonProperty("project_id")
    private String projectId;
}
