package com.elocate.elocate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletBalanceResponse {

    private BigDecimal pointsBalance;
    private BigDecimal lifetimeEarned;
    private Integer totalTransactions;
    private BigDecimal monetaryAmount;
    private String currencyCode;
    private BigDecimal conversionRate;
}
