package com.elocate.elocate.repository;

import com.elocate.elocate.dto.FacilityWithDistanceProjection;
import com.elocate.elocate.model.RecyclingFacility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

import com.elocate.elocate.model.User;

@Repository
public interface RecyclingFacilityRepository extends JpaRepository<RecyclingFacility, UUID> {

    @Query(value = """
                SELECT
                    f.id AS id,
                    f.name AS name,
                    f.address AS address,
                    f.capacity AS capacity,
                    f.latitude AS latitude,
                    f.longitude AS longitude,
                    u.mobile_number AS contactNumber,
                    f.operating_hours AS operatingHours,
                    f.is_verified AS isVerified,
                    (6371 * acos(
                        cos(radians(:lat)) *
                        cos(radians(f.latitude)) *
                        cos(radians(f.longitude) - radians(:lon)) +
                        sin(radians(:lat)) *
                        sin(radians(f.latitude))
                    )) AS distance
                FROM recycling_facility f
                LEFT JOIN "user" u ON u.id = f.user_id
                WHERE f.is_active = true
                AND (6371 * acos(
                    cos(radians(:lat)) *
                    cos(radians(f.latitude)) *
                    cos(radians(f.longitude) - radians(:lon)) +
                    sin(radians(:lat)) *
                    sin(radians(f.latitude))
                )) <= :distance
                ORDER BY distance
            """, countQuery = """
                SELECT count(*)
                FROM recycling_facility
                WHERE is_active = true
                AND (6371 * acos(
                    cos(radians(:lat)) *
                    cos(radians(latitude)) *
                    cos(radians(longitude) - radians(:lon)) +
                    sin(radians(:lat)) *
                    sin(radians(latitude))
                )) <= :distance
            """, nativeQuery = true)
    Page<FacilityWithDistanceProjection> findNearestFacilities(
            @Param("lat") double lat,
            @Param("lon") double lon,
            @Param("distance") double distance,
            Pageable pageable);

    @Query(value = """
                SELECT
                    f.id AS id,
                    f.name AS name,
                    f.address AS address,
                    f.capacity AS capacity,
                    f.latitude AS latitude,
                    f.longitude AS longitude,
                    u.mobile_number AS contactNumber,
                    f.operating_hours AS operatingHours,
                    f.is_verified AS isVerified,
                    (6371 * acos(
                        cos(radians(:lat)) *
                        cos(radians(f.latitude)) *
                        cos(radians(f.longitude) - radians(:lon)) +
                        sin(radians(:lat)) *
                        sin(radians(f.latitude))
                    )) AS distance
                FROM recycling_facility f
                LEFT JOIN "user" u ON u.id = f.user_id
                WHERE f.is_active = true
                AND f.is_verified = true
                AND (6371 * acos(
                    cos(radians(:lat)) *
                    cos(radians(f.latitude)) *
                    cos(radians(f.longitude) - radians(:lon)) +
                    sin(radians(:lat)) *
                    sin(radians(f.latitude))
                )) <= :distance
                ORDER BY distance
            """, countQuery = """
                SELECT count(*)
                FROM recycling_facility
                WHERE is_active = true
                AND is_verified = true
                AND (6371 * acos(
                    cos(radians(:lat)) *
                    cos(radians(latitude)) *
                    cos(radians(longitude) - radians(:lon)) +
                    sin(radians(:lat)) *
                    sin(radians(latitude))
                )) <= :distance
            """, nativeQuery = true)
    Page<FacilityWithDistanceProjection> findNearestVerifiedFacilities(
            @Param("lat") double lat,
            @Param("lon") double lon,
            @Param("distance") double distance,
            Pageable pageable);

    Page<RecyclingFacility> findByNameContainingIgnoreCaseOrAddressContainingIgnoreCase(
            String name, String address, Pageable pageable);

    Optional<RecyclingFacility> findByRegistrationNumber(String registrationNumber);

    Page<RecyclingFacility> findByIsVerified(Boolean isVerified, Pageable pageable);

    Page<RecyclingFacility> findByNameContainingIgnoreCaseOrRegistrationNumberContainingIgnoreCase(
            String name, String registrationNumber, Pageable pageable);

    Page<RecyclingFacility> findByNameContainingIgnoreCaseOrRegistrationNumberContainingIgnoreCaseAndIsVerified(
            String name, String registrationNumber, Boolean isVerified, Pageable pageable);

    Optional<RecyclingFacility> findByUser(User user);

    Optional<RecyclingFacility> findByUserId(UUID userId);

    Page<RecyclingFacility> findByApprovalStatus(String approvalStatus, Pageable pageable);

}