package com.elocate.elocate.repository;

import com.elocate.elocate.model.DeviceBrand;
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
public interface DeviceBrandRepository extends JpaRepository<DeviceBrand, UUID> {

       // Find by code (case-insensitive)
       Optional<DeviceBrand> findByCodeIgnoreCase(String code);

       // Find all active brands
       Page<DeviceBrand> findByIsActive(Boolean isActive, Pageable pageable);

       // Search by name (case-insensitive, partial match)
       Page<DeviceBrand> findByNameContainingIgnoreCase(String name, Pageable pageable);

       // Search by code (case-insensitive, partial match)
       Page<DeviceBrand> findByCodeContainingIgnoreCase(String code, Pageable pageable);

       // Combined search across name, code, and description
       @Query("SELECT db FROM DeviceBrand db WHERE " +
                     "LOWER(db.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                     "LOWER(db.code) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                     "LOWER(db.description) LIKE LOWER(CONCAT('%', :search, '%'))")
       Page<DeviceBrand> searchBrands(@Param("search") String search, Pageable pageable);

       // Combined search with active filter
       @Query("SELECT db FROM DeviceBrand db WHERE " +
                     "db.isActive = :isActive AND (" +
                     "LOWER(db.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                     "LOWER(db.code) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                     "LOWER(db.description) LIKE LOWER(CONCAT('%', :search, '%')))")
       Page<DeviceBrand> searchBrandsWithActiveFilter(
                     @Param("search") String search,
                     @Param("isActive") Boolean isActive,
                     Pageable pageable);

       // Find by active status with sorting by name
       Page<DeviceBrand> findByIsActiveOrderByNameAsc(Boolean isActive, Pageable pageable);

       // All brands sorted by name
       @Query("SELECT db FROM DeviceBrand db ORDER BY db.name ASC")
       Page<DeviceBrand> findAllByOrderByNameAsc(Pageable pageable);
}
