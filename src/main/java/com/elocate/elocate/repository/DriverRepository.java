package com.elocate.elocate.repository;

import com.elocate.elocate.model.Driver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DriverRepository extends JpaRepository<Driver, UUID> {
        Optional<Driver> findByEmail(String email);

        Page<Driver> findByAvailabilityIgnoreCase(String availability, Pageable pageable);

        Page<Driver> findByFacilityId(UUID facilityId, Pageable pageable);

        Page<Driver> findByFacilityIdAndAvailabilityIgnoreCase(UUID facilityId, String availability, Pageable pageable);

        @Query("SELECT d FROM Driver d WHERE d.facilityId = :facilityId AND (" +
                        "LOWER(d.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "LOWER(d.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "LOWER(d.phone) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "LOWER(d.vehicleNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
        Page<Driver> searchDriversByFacility(@Param("facilityId") UUID facilityId,
                        @Param("searchTerm") String searchTerm, Pageable pageable);

        @Query("SELECT d FROM Driver d WHERE d.facilityId = :facilityId AND " +
                        "LOWER(d.availability) = LOWER(:availability) AND (" +
                        "LOWER(d.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "LOWER(d.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "LOWER(d.phone) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "LOWER(d.vehicleNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
        Page<Driver> findByFacilityIdAndAvailabilityAndSearchTerm(@Param("facilityId") UUID facilityId,
                        @Param("availability") String availability,
                        @Param("searchTerm") String searchTerm, Pageable pageable);

        @Query("SELECT d FROM Driver d WHERE " +
                        "LOWER(d.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "LOWER(d.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "LOWER(d.phone) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "LOWER(d.vehicleNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
        Page<Driver> searchDrivers(@Param("searchTerm") String searchTerm, Pageable pageable);

        @Query("SELECT d FROM Driver d WHERE " +
                        "LOWER(d.availability) = LOWER(:availability) AND (" +
                        "LOWER(d.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "LOWER(d.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "LOWER(d.phone) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "LOWER(d.vehicleNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
        Page<Driver> findByAvailabilityAndSearchTerm(@Param("availability") String availability,
                        @Param("searchTerm") String searchTerm, Pageable pageable);
}
