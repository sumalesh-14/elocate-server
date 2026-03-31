package com.elocate.elocate.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletStatsResponse {
    private BigDecimal totalAmount;             // total INR earned (wallet balance)
    private BigDecimal highestSingleAmount;     // highest single transaction in INR
    private int totalTransactions;
    private int userRank;
    private long totalUsersRanked;
    private String rankTier;
    private List<WalletTransactionResponse> transactions;
}
