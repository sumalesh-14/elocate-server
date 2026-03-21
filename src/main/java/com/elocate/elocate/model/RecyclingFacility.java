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

    @Column(nullable = false, columnDefinition = "TEXT")
    private String address;

    @Column(nullable = false, precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 11, scale = 8)
    private BigDecimal longitude;

    private Integer capacity;

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

    @Size(max = 100)
    @Column(name = "state", length = 100)
    private String state;

    @Size(max = 20)
    @Column(name = "pincode", length = 20)
    private String pincode;

    @Size(max = 100)
    @Column(name = "registration_number", length = 100, unique = true)
    private String registrationNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder.Default
    @Column(name = "approval_status", length = 20)
    private String approvalStatus = "PENDING";

    @Column(name = "document_url", columnDefinition = "TEXT")
    private String documentUrl;

    @Column(name = "document_urls", columnDefinition = "TEXT[]")
    private String[] documentUrls;

    @Builder.Default
    @OneToMany(mappedBy = "facility")
    private Set<FacilityServiceArea> facilityServiceAreas = new LinkedHashSet<>();

}