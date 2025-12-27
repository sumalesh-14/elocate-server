package com.elocate.elocate.repository;

import com.elocate.elocate.model.DeviceModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DeviceModelRepository extends JpaRepository<DeviceModel, UUID> {
    
    // Find all active models
    List<DeviceModel> findByIsActive(Boolean isActive);
    
    // Search by model name (case-insensitive, partial match)
    List<DeviceModel> findByModelNameContainingIgnoreCase(String modelName);
    
    // Find by category ID
    @Query("SELECT dm FROM DeviceModel dm WHERE dm.category.id = :categoryId")
    List<DeviceModel> findByCategoryId(@Param("categoryId") UUID categoryId);
    
    // Find by brand ID
    @Query("SELECT dm FROM DeviceModel dm WHERE dm.brand.id = :brandId")
    List<DeviceModel> findByBrandId(@Param("brandId") UUID brandId);
    
    // Find by category and brand
    @Query("SELECT dm FROM DeviceModel dm WHERE dm.category.id = :categoryId AND dm.brand.id = :brandId")
    List<DeviceModel> findByCategoryIdAndBrandId(
            @Param("categoryId") UUID categoryId, 
            @Param("brandId") UUID brandId
    );
    
    // Combined search across model name
    @Query("SELECT dm FROM DeviceModel dm WHERE " +
           "LOWER(dm.modelName) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<DeviceModel> searchModels(@Param("search") String search);
    
    // Combined search with active filter
    @Query("SELECT dm FROM DeviceModel dm WHERE " +
           "dm.isActive = :isActive AND " +
           "LOWER(dm.modelName) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<DeviceModel> searchModelsWithActiveFilter(
            @Param("search") String search, 
            @Param("isActive") Boolean isActive
    );
    
    // Find by active status with sorting by model name
    List<DeviceModel> findByIsActiveOrderByModelNameAsc(Boolean isActive);
    
    // Search with category filter
    @Query("SELECT dm FROM DeviceModel dm WHERE " +
           "dm.category.id = :categoryId AND " +
           "LOWER(dm.modelName) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<DeviceModel> searchModelsByCategory(
            @Param("categoryId") UUID categoryId,
            @Param("search") String search
    );
    
    // Search with brand filter
    @Query("SELECT dm FROM DeviceModel dm WHERE " +
           "dm.brand.id = :brandId AND " +
           "LOWER(dm.modelName) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<DeviceModel> searchModelsByBrand(
            @Param("brandId") UUID brandId,
            @Param("search") String search
    );
}
