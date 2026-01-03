package com.elocate.elocate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryBrandResponse {
    
    private UUID id;
    private DeviceCategoryResponse category;
    private DeviceBrandResponse brand;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
