package com.elocate.elocate.dto;

import com.elocate.elocate.model.WalletTransaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO for citizen dashboard
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CitizenDashboardResponse {
    private Long totalRequests;
    private Long completedRequests;
    private BigDecimal walletBalance;
    private String currencyCode;
    private List<RecycleRequestResponse> recentRequests;
    private List<WalletTransaction> recentTransactions;
    private EnvironmentalImpact environmentalImpact;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EnvironmentalImpact {
        private Long devicesRecycled;
        private Double co2SavedKg;
    }
}
