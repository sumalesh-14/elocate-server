package com.elocate.elocate.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletTransactionResponse {
    private UUID id;
    private String transactionType;
    private BigDecimal amount;
    private String description;
    private String recycleRequestNumber;
    private String citizenName;
    private LocalDateTime createdAt;
}
