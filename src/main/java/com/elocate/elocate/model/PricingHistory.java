package com.elocate.elocate.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pricing_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PricingHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "recycle_request_id", nullable = false)
    private UUID recycleRequestId;

    @Column(name = "field_name", nullable = false, length = 50)
    private String fieldName;

    @Column(name = "old_value", precision = 10, scale = 2)
    private BigDecimal oldValue;

    @Column(name = "new_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal newValue;

    @Column(name = "changed_by", nullable = false)
    private UUID changedBy;

    @Column(name = "change_reason", nullable = false)
    private String changeReason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
