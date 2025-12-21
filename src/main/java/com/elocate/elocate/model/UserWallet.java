package com.elocate.elocate.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_wallet")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserWallet {
    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Builder.Default
    @Column(name = "points_balance")
    private Integer pointsBalance = 0;

    @Column(name = "last_updated_at")
    private LocalDateTime lastUpdatedAt;
}