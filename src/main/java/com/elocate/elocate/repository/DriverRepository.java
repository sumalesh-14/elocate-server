package com.elocate.elocate.repository;

import com.elocate.elocate.model.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DriverRepository extends JpaRepository<Driver, UUID> {
    Optional<Driver> findByEmail(String email);

    List<Driver> findByAvailabilityIgnoreCase(String availability);

    @Query("SELECT d FROM Driver d WHERE " +
            "LOWER(d.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(d.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(d.phone) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(d.vehicleNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Driver> searchDrivers(@Param("searchTerm") String searchTerm);

    @Query("SELECT d FROM Driver d WHERE " +
            "LOWER(d.availability) = LOWER(:availability) AND (" +
            "LOWER(d.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(d.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(d.phone) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(d.vehicleNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Driver> findByAvailabilityAndSearchTerm(@Param("availability") String availability,
            @Param("searchTerm") String searchTerm);
}
