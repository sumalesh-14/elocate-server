package com.elocate.elocate.repository;

import com.elocate.elocate.model.DeviceModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DeviceModelRepository extends JpaRepository<DeviceModel, UUID> {

        // Find all active models
        Page<DeviceModel> findByIsActive(Boolean isActive, Pageable pageable);

        // Search by model name (case-insensitive, partial match)
        Page<DeviceModel> findByModelNameContainingIgnoreCase(String modelName, Pageable pageable);

        // Find by category ID
        @Query("SELECT dm FROM DeviceModel dm WHERE dm.category.id = :categoryId")
        Page<DeviceModel> findByCategoryId(@Param("categoryId") UUID categoryId, Pageable pageable);

        // Find by brand ID
        @Query("SELECT dm FROM DeviceModel dm WHERE dm.brand.id = :brandId")
        Page<DeviceModel> findByBrandId(@Param("brandId") UUID brandId, Pageable pageable);

        // Find by category and brand
        @Query("SELECT dm FROM DeviceModel dm WHERE dm.category.id = :categoryId AND dm.brand.id = :brandId")
        Page<DeviceModel> findByCategoryIdAndBrandId(
                        @Param("categoryId") UUID categoryId,
                        @Param("brandId") UUID brandId,
                        Pageable pageable);

        // Combined search across model name
        @Query("SELECT dm FROM DeviceModel dm WHERE " +
                        "LOWER(dm.modelName) LIKE LOWER(CONCAT('%', :search, '%'))")
        Page<DeviceModel> searchModels(@Param("search") String search, Pageable pageable);

        // Combined search with active filter
        @Query("SELECT dm FROM DeviceModel dm WHERE " +
                        "dm.isActive = :isActive AND " +
                        "LOWER(dm.modelName) LIKE LOWER(CONCAT('%', :search, '%'))")
        Page<DeviceModel> searchModelsWithActiveFilter(
                        @Param("search") String search,
                        @Param("isActive") Boolean isActive,
                        Pageable pageable);

        // Find by active status with sorting by model name
        Page<DeviceModel> findByIsActiveOrderByModelNameAsc(Boolean isActive, Pageable pageable);

        // Search with category filter
        @Query("SELECT dm FROM DeviceModel dm WHERE " +
                        "dm.category.id = :categoryId AND " +
                        "LOWER(dm.modelName) LIKE LOWER(CONCAT('%', :search, '%'))")
        Page<DeviceModel> searchModelsByCategory(
                        @Param("categoryId") UUID categoryId,
                        @Param("search") String search,
                        Pageable pageable);

        // Search with brand filter
        @Query("SELECT dm FROM DeviceModel dm WHERE " +
                        "dm.brand.id = :brandId AND " +
                        "LOWER(dm.modelName) LIKE LOWER(CONCAT('%', :search, '%'))")
        Page<DeviceModel> searchModelsByBrand(
                        @Param("brandId") UUID brandId,
                        @Param("search") String search,
                        Pageable pageable);

        // All models sorted by name
        @Query("SELECT dm FROM DeviceModel dm ORDER BY dm.modelName ASC")
        Page<DeviceModel> findAllByOrderByModelNameAsc(Pageable pageable);
}
