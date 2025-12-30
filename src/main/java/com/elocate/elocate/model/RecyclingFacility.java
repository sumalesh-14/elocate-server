package com.elocate.elocate.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "recycling_facility")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecyclingFacility {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false, precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 11, scale = 8)
    private BigDecimal longitude;

    private Integer capacity;

    @Column(name = "contact_number")
    private String contactNumber;

    @Column(name = "operating_hours")
    private String operatingHours;

    @Builder.Default
    @Column(name = "is_verified")
    private Boolean isVerified = false;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Size(max = 50)
    @Column(name = "geocode_source", length = 50)
    private String geocodeSource;

    @Column(name = "email", length = Integer.MAX_VALUE)
    private String email;

    @Size(max = 100)
    @Column(name = "state", length = 100)
    private String state;

    @Size(max = 20)
    @Column(name = "pincode", length = 20)
    private String pincode;

    @OneToMany(mappedBy = "facility")
    private Set<FacilityServiceArea> facilityServiceAreas = new LinkedHashSet<>();

}