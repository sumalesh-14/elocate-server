package com.elocate.elocate.repository;

import com.elocate.elocate.model.DeviceBrand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeviceBrandRepository extends JpaRepository<DeviceBrand, UUID> {
    
    // Find by code (case-insensitive)
    Optional<DeviceBrand> findByCodeIgnoreCase(String code);
    
    // Find all active brands
    List<DeviceBrand> findByIsActive(Boolean isActive);
    
    // Search by name (case-insensitive, partial match)
    List<DeviceBrand> findByNameContainingIgnoreCase(String name);
    
    // Search by code (case-insensitive, partial match)
    List<DeviceBrand> findByCodeContainingIgnoreCase(String code);
    
    // Combined search across name, code, and description
    @Query("SELECT db FROM DeviceBrand db WHERE " +
           "LOWER(db.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(db.code) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(db.description) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<DeviceBrand> searchBrands(@Param("search") String search);
    
    // Combined search with active filter
    @Query("SELECT db FROM DeviceBrand db WHERE " +
           "db.isActive = :isActive AND (" +
           "LOWER(db.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(db.code) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(db.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<DeviceBrand> searchBrandsWithActiveFilter(
            @Param("search") String search, 
            @Param("isActive") Boolean isActive
    );
    
    // Find by active status with sorting by name
    List<DeviceBrand> findByIsActiveOrderByNameAsc(Boolean isActive);
}
