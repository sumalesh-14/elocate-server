package com.elocate.elocate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceBrandRequest {
    
    @NotBlank(message = "Brand code is required")
    @Size(max = 50, message = "Brand code must not exceed 50 characters")
    private String code;
    
    @NotBlank(message = "Brand name is required")
    @Size(max = 100, message = "Brand name must not exceed 100 characters")
    private String name;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    
    @Builder.Default
    private Boolean isActive = true;
}
