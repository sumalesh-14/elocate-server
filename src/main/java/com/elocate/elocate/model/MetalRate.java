package com.elocate.elocate.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "metal_rate")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetalRate {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "metal_type", nullable = false)
    private String metalType;

    @Column(nullable = false)
    private String unit;

    @Column(name = "points_per_unit", precision = 10, scale = 2)
    private BigDecimal pointsPerUnit;

    @Column(name = "effective_from")
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;
}