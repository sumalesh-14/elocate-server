package com.elocate.elocate.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "recycle_verification")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecycleVerification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recycle_request_id", nullable = false)
    private RecycleRequest recycleRequest;

    @Column(name = "verified_weight_grams", precision = 10, scale = 2)
    private BigDecimal verifiedWeightGrams;

    @Column(name = "verified_condition")
    private String verifiedCondition;

    private String remarks;

    @Column(name = "verified_by")
    private UUID verifiedBy;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;
}