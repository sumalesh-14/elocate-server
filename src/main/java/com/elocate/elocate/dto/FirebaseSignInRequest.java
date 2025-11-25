package com.elocate.elocate.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FirebaseSignInRequest {
    @NotBlank
    @Email
    private String email;
    
    @NotBlank
    private String password;
    
    private boolean returnSecureToken = true;
}