package com.elocate.elocate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceModelRequest {
    
    @NotBlank(message = "Model name is required")
    @Size(max = 100, message = "Model name must not exceed 100 characters")
    private String modelName;
    
    @NotNull(message = "Category ID is required")
    private UUID categoryId;
    
    @NotNull(message = "Brand ID is required")
    private UUID brandId;
    
    private Integer releaseYear;
    
    private BigDecimal avgWeightGrams;
    
    private BigDecimal goldMg;
    
    private BigDecimal silverMg;
    
    private BigDecimal copperG;
    
    private BigDecimal palladiumMg;
    
    private Integer recyclabilityScore;
    
    private Integer basePoints;
    
    @Builder.Default
    private Boolean isActive = true;
}
