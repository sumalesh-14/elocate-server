package com.elocate.elocate.dto;

import com.elocate.elocate.model.enums.OtpType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for resending OTP
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResendOtpDto {
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    @NotNull(message = "OTP type is required")
    private OtpType type;
}
