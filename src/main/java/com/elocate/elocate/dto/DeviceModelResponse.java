package com.elocate.elocate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceModelResponse {
    
    private UUID id;
    private String modelName;
    private UUID categoryId;
    private String categoryName;
    private String categoryCode;
    private UUID brandId;
    private String brandName;
    private String brandCode;
    private Integer releaseYear;
    private BigDecimal avgWeightGrams;
    private BigDecimal goldMg;
    private BigDecimal silverMg;
    private BigDecimal copperG;
    private BigDecimal palladiumMg;
    private Integer recyclabilityScore;
    private Integer basePoints;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
