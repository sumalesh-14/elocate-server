package com.elocate.elocate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenValidationResponse {
    private Boolean valid;
    private String action; // ACCEPT or REJECT
    private String requestId;
    private String driverName;
    private String citizenAddress;
    private String deviceName;
    private String message;
}
