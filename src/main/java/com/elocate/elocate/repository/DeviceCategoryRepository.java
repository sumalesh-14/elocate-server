package com.elocate.elocate.repository;

import com.elocate.elocate.model.DeviceCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
       Page<DeviceCategory> findByNameContainingIgnoreCase(String name, Pageable pageable);

       // Search by code (case-insensitive, partial match)
       Page<DeviceCategory> findByCodeContainingIgnoreCase(String code, Pageable pageable);

       // Combined search across name, code, and description
       @Query("SELECT dc FROM DeviceCategory dc WHERE " +
                     "LOWER(dc.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                     "LOWER(dc.code) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                     "LOWER(dc.description) LIKE LOWER(CONCAT('%', :search, '%'))")
       Page<DeviceCategory> searchCategories(@Param("search") String search, Pageable pageable);

       // Combined search with active filter
       @Query("SELECT dc FROM DeviceCategory dc WHERE " +
                     "dc.isActive = :isActive AND (" +
                     "LOWER(dc.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                     "LOWER(dc.code) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                     "LOWER(dc.description) LIKE LOWER(CONCAT('%', :search, '%')))")
       Page<DeviceCategory> searchCategoriesWithActiveFilter(
                     @Param("search") String search,
                     @Param("isActive") Boolean isActive,
                     Pageable pageable);

       // Find by active status with sorting by name
       Page<DeviceCategory> findByIsActiveOrderByNameAsc(Boolean isActive, Pageable pageable);

       // All categories with pagination
       Page<DeviceCategory> findAll(Pageable pageable);
}
