package com.elocate.elocate.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "recycle_status_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecycleStatusHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recycle_request_id", nullable = false)
    private RecycleRequest recycleRequest;

    @Column(name = "old_status")
    private String oldStatus;

    @Column(name = "new_status")
    private String newStatus;

    @Column(name = "changed_by")
    private UUID changedBy;

    @Column(name = "changed_at")
    private LocalDateTime changedAt;
}