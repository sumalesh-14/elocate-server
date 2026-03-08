package com.elocate.elocate.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class AssignDriverDto {
    @NotNull(message = "Driver ID is required")
    private UUID driverId;
    
    private String comments; // Comments to be sent to driver and stored in history
}
