package com.elocate.elocate.repository;

import com.elocate.elocate.model.DeviceCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeviceCategoryRepository extends JpaRepository<DeviceCategory, UUID> {
    
    // Find by code (case-insensitive)
    Optional<DeviceCategory> findByCodeIgnoreCase(String code);
    
    // Find all active categories
    List<DeviceCategory> findByIsActive(Boolean isActive);
    
    // Search by name (case-insensitive, partial match)
    List<DeviceCategory> findByNameContainingIgnoreCase(String name);
    
    // Search by code (case-insensitive, partial match)
    List<DeviceCategory> findByCodeContainingIgnoreCase(String code);
    
    // Combined search across name, code, and description
    @Query("SELECT dc FROM DeviceCategory dc WHERE " +
           "LOWER(dc.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(dc.code) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(dc.description) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<DeviceCategory> searchCategories(@Param("search") String search);
    
    // Combined search with active filter
    @Query("SELECT dc FROM DeviceCategory dc WHERE " +
           "dc.isActive = :isActive AND (" +
           "LOWER(dc.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(dc.code) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(dc.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<DeviceCategory> searchCategoriesWithActiveFilter(
            @Param("search") String search, 
            @Param("isActive") Boolean isActive
    );
    
    // Find by active status with sorting by name
    List<DeviceCategory> findByIsActiveOrderByNameAsc(Boolean isActive);
}
