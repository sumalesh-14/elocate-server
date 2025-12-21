package com.elocate.elocate.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "facility_service_area")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacilityServiceArea {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id", nullable = false)
    private RecyclingFacility facility;

    private String pincode;
    private String city;
    private String state;
}