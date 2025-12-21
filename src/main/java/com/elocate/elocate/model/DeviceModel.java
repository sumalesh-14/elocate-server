package com.elocate.elocate.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "device_model")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceModel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private DeviceCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private DeviceBrand brand;

    @Column(name = "model_name", nullable = false)
    private String modelName;

    @Column(name = "release_year")
    private Integer releaseYear;

    @Column(name = "avg_weight_grams", precision = 10, scale = 2)
    private BigDecimal avgWeightGrams;

    @Column(name = "gold_mg", precision = 10, scale = 2)
    private BigDecimal goldMg;

    @Column(name = "silver_mg", precision = 10, scale = 2)
    private BigDecimal silverMg;

    @Column(name = "copper_g", precision = 10, scale = 2)
    private BigDecimal copperG;

    @Column(name = "palladium_mg", precision = 10, scale = 2)
    private BigDecimal palladiumMg;

    @Column(name = "recyclability_score")
    private Integer recyclabilityScore;

    @Column(name = "base_points")
    private Integer basePoints;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}