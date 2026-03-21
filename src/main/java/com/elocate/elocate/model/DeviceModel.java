package com.elocate.elocate.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "recycle_items", columnDefinition = "jsonb")
    private String recycleItems;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "ai_base_price", precision = 10, scale = 2)
    private java.math.BigDecimal aiBasePrice;

    @Column(name = "manual_price_override", precision = 10, scale = 2)
    private java.math.BigDecimal manualPriceOverride;

    @Column(name = "price_override_reason")
    private String priceOverrideReason;

    @Column(name = "price_override_by")
    private UUID priceOverrideBy;

    @Column(name = "price_override_at")
    private LocalDateTime priceOverrideAt;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}