package com.elocate.elocate.repository;

import com.elocate.elocate.dto.FacilityWithDistanceProjection;
import com.elocate.elocate.model.RecyclingFacility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elocate.elocate.model.User;

@Repository
public interface RecyclingFacilityRepository extends JpaRepository<RecyclingFacility, UUID> {

    @Query(value = """
    SELECT 
        f.name AS name,
        f.address AS address,
        f.capacity AS capacity,
        f.latitude AS latitude,
        f.longitude AS longitude,
        f.contact_number AS contactNumber,
        f.operating_hours AS operatingHours,
        f.is_verified AS isVerified,
        6371 * acos(
            cos(radians(:lat)) *
            cos(radians(f.latitude)) *
            cos(radians(f.longitude) - radians(:lon)) +
            sin(radians(:lat)) *
            sin(radians(f.latitude))
        ) AS distance
    FROM recycling_facility f
    WHERE f.is_active = true
    HAVING distance <= :distance
    ORDER BY distance
    LIMIT 10
""", nativeQuery = true)
    List<FacilityWithDistanceProjection> findNearestFacilities(
            @Param("lat") double lat,
            @Param("lon") double lon,
            @Param("distance") double distance
    );

    Page<RecyclingFacility> findByNameContainingIgnoreCaseOrAddressContainingIgnoreCase(
            String name, String address, Pageable pageable);

    Optional<RecyclingFacility> findByRegistrationNumber(String registrationNumber);

    Page<RecyclingFacility> findByIsVerified(Boolean isVerified, Pageable pageable);

    Page<RecyclingFacility> findByNameContainingIgnoreCaseOrRegistrationNumberContainingIgnoreCase(
            String name, String registrationNumber, Pageable pageable);

    Page<RecyclingFacility> findByNameContainingIgnoreCaseOrRegistrationNumberContainingIgnoreCaseAndIsVerified(
            String name, String registrationNumber, Boolean isVerified, Pageable pageable);

    Optional<RecyclingFacility> findByUser(User user);

    Page<RecyclingFacility> findByApprovalStatus(String approvalStatus, Pageable pageable);

}