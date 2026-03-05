package com.elocate.elocate.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DriverActionReasonDto {
    @NotBlank(message = "Reason cannot be empty")
    private String reason;
}
