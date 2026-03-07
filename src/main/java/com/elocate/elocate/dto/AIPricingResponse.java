package com.elocate.elocate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO from AI Pricing API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIPricingResponse {
    private BigDecimal estimatedPrice;
    private List<PreciousMaterial> preciousMaterials;
    private String confidence;
    private String notes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PreciousMaterial {
        private String type;  // GOLD, SILVER, COPPER, PALLADIUM
        private BigDecimal quantity;
        private String unit;  // grams, mg
        private BigDecimal value;
    }
}
