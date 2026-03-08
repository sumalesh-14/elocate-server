package com.elocate.elocate.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DriverPickupRejectDto {
    
    @NotBlank(message = "Reason is required")
    private String reason;
}
