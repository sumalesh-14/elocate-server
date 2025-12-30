package com.elocate.elocate.model;

import com.elocate.elocate.model.enums.FulfillmentStatus;
import com.elocate.elocate.model.enums.FulfillmentType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "recycle_request")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecycleRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_model_id", nullable = false)
    private DeviceModel deviceModel;

    @Column(name = "condition_code", nullable = false)
    private String conditionCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recycling_facility_id")
    private RecyclingFacility recyclingFacility;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pickup_address_id")
    private UserAddress pickupAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "fulfillment_type", nullable = false)
    private FulfillmentType fulfillmentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "fulfillment_status", nullable = false)
    private FulfillmentStatus fulfillmentStatus;

    @Column(name = "estimated_points", precision = 10, scale = 2)
    private BigDecimal estimatedPoints;

    @Column(name = "final_points", precision = 10, scale = 2)
    private BigDecimal finalPoints;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecycleStatus status;

    @Column(name = "pickup_date")
    private LocalDate pickupDate;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}