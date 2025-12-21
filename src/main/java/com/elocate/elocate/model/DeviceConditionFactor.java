package com.elocate.elocate.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "device_condition_factor")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceConditionFactor {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "condition_code", nullable = false, unique = true)
    private String conditionCode;

    @Column(precision = 10, scale = 2)
    private BigDecimal multiplier;

    private String description;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;
}