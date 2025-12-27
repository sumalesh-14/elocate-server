package com.elocate.elocate.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reward_snapshot")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RewardSnapshot {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recycle_request_id", nullable = false)
    private RecycleRequest recycleRequest;

    @Column(name = "gold_mg", precision = 10, scale = 2)
    private BigDecimal goldMg;

    @Column(name = "silver_mg", precision = 10, scale = 2)
    private BigDecimal silverMg;

    @Column(name = "copper_g", precision = 10, scale = 2)
    private BigDecimal copperG;

    @Column(name = "palladium_mg", precision = 10, scale = 2)
    private BigDecimal palladiumMg;

    @Column(name = "condition_multiplier", precision = 10, scale = 2)
    private BigDecimal conditionMultiplier;

    @Column(name = "total_points")
    private BigDecimal totalPoints;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}