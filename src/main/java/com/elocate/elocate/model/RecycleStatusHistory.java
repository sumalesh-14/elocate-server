package com.elocate.elocate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @JsonIgnore // Prevent serialization of the lazy-loaded relationship
    private RecycleRequest recycleRequest;

    @Column(name = "old_status")
    private String oldStatus;

    @Column(name = "new_status")
    private String newStatus;

    @Column(name = "status_type")
    private String statusType; // "RECYCLE_STATUS" or "FULFILLMENT_STATUS"

    @Column(name = "changed_by")
    private UUID changedBy;

    @Column(name = "changed_by_name")
    private String changedByName;

    @Column(name = "changed_at")
    private LocalDateTime changedAt;

    @Column(name = "comments", length = 1000)
    private String comments;
}