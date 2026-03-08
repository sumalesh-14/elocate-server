package com.elocate.elocate.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DriverPickupActionDto {
    
    @NotBlank(message = "Comments are required")
    private String comments;
    
    private String photoUrl; // S3 URL of uploaded photo
}
