package com.elocate.elocate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FirebaseSignInResponse {
    private String idToken;
    private String refreshToken;
    private String localId;
    private String email;
}