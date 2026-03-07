package com.elocate.elocate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Detailed pricing breakdown response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PricingBreakdownResponse {
    private BigDecimal basePrice;
    private BigDecimal conditionMultiplier;
    private BigDecimal conditionAdjustedPrice;
    private BigDecimal materialsValue;
    private BigDecimal totalEstimatedPoints;
    private BigDecimal monetaryAmount;
    private String currencyCode;
    private List<MaterialValueBreakdown> materialBreakdown;
    private String pricingSource; // "AI", "MANUAL_OVERRIDE", "DATABASE"

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MaterialValueBreakdown {
        private String metalType;
        private BigDecimal quantity;
        private String unit;
        private BigDecimal ratePerUnit;
        private BigDecimal totalValue;
    }
}
