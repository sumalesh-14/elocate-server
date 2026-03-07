package com.elocate.elocate.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "facility_reassignment")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacilityReassignment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "recycle_request_id", nullable = false)
    private UUID recycleRequestId;

    @Column(name = "old_facility_id")
    private UUID oldFacilityId;

    @Column(name = "new_facility_id", nullable = false)
    private UUID newFacilityId;

    @Column(name = "reassigned_by", nullable = false)
    private UUID reassignedBy;

    @Column(name = "reason", nullable = false)
    private String reason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
