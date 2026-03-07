package com.elocate.elocate.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "wallet_transaction")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recycle_request_id")
    private RecycleRequest recycleRequest;

    @Column(name = "transaction_type")
    private String transactionType;

    private BigDecimal points;

    @Column(name = "conversion_rate", precision = 10, scale = 4)
    private BigDecimal conversionRate;

    @Column(name = "monetary_amount", precision = 10, scale = 2)
    private BigDecimal monetaryAmount;

    private String description;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}