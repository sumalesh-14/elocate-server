package com.elocate.elocate.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Secure token for driver pickup email links
 */
@Entity
@Table(name = "driver_pickup_tokens")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverPickupToken {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "recycle_request_id", nullable = false)
    private UUID recycleRequestId;

    @Column(name = "driver_id", nullable = false)
    private UUID driverId;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(name = "action_type", nullable = false, length = 20)
    private String actionType; // ACCEPT or REJECT

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    @Builder.Default
    private Boolean used = false;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return (used == null || !used) && !isExpired() && (isActive == null || isActive);
    }
}
