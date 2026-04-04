package com.elocate.elocate.dto;

import com.elocate.elocate.model.WithdrawalStatus;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawalRequestResponse {
    private UUID id;
    private BigDecimal amount;
    private String accountHolderName;
    private String maskedAccountNumber;
    private String fullAccountNumber;
    private String bankName;
    private String ifscCode;
    private String mobileNumber;
    private String upiId;
    private String email;
    private WithdrawalStatus status;
    private String adminNote;
    private LocalDateTime requestedAt;
    private LocalDateTime processedAt;
    // enriched for intermediary view
    private String citizenName;
    private String citizenEmail;
}
