package com.elocate.elocate.service;

import com.elocate.elocate.dto.DeviceModelRequest;
import com.elocate.elocate.dto.DeviceModelResponse;
import com.elocate.elocate.exception.BrandNotFoundException;
import com.elocate.elocate.exception.CategoryNotFoundException;
import com.elocate.elocate.exception.ModelNotFoundException;
import com.elocate.elocate.model.DeviceBrand;
import com.elocate.elocate.model.DeviceCategory;
import com.elocate.elocate.model.DeviceModel;
import com.elocate.elocate.repository.DeviceBrandRepository;
import com.elocate.elocate.repository.DeviceCategoryRepository;
import com.elocate.elocate.repository.DeviceModelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class DeviceModelService {
    
    private final DeviceModelRepository modelRepository;
    private final DeviceCategoryRepository categoryRepository;
    private final DeviceBrandRepository brandRepository;
    
    /**
     * Get all models with optional search and filters
     */
    @Transactional(readOnly = true)
    public List<DeviceModelResponse> getAllModels(String search, Boolean isActive, UUID categoryId, UUID brandId) {
        log.info("Fetching models with search: {}, isActive: {}, categoryId: {}, brandId: {}", 
                search, isActive, categoryId, brandId);
        
        List<DeviceModel> models;
        
        // Apply filters based on provided parameters
        if (categoryId != null && brandId != null) {
            models = modelRepository.findByCategoryIdAndBrandId(categoryId, brandId);
        } else if (categoryId != null) {
            if (search != null && !search.trim().isEmpty()) {
                models = modelRepository.searchModelsByCategory(categoryId, search);
            } else {
                models = modelRepository.findByCategoryId(categoryId);
            }
        } else if (brandId != null) {
            if (search != null && !search.trim().isEmpty()) {
                models = modelRepository.searchModelsByBrand(brandId, search);
            } else {
                models = modelRepository.findByBrandId(brandId);
            }
        } else if (search != null && !search.trim().isEmpty()) {
            if (isActive != null) {
                models = modelRepository.searchModelsWithActiveFilter(search, isActive);
            } else {
                models = modelRepository.searchModels(search);
            }
        } else if (isActive != null) {
            models = modelRepository.findByIsActiveOrderByModelNameAsc(isActive);
        } else {
            models = modelRepository.findAll();
        }
        
        return models.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get model by ID
     */
    @Transactional(readOnly = true)
    public DeviceModelResponse getModelById(UUID id) {
        log.info("Fetching model by id: {}", id);
        DeviceModel model = modelRepository.findById(id)
                .orElseThrow(() -> new ModelNotFoundException(id));
        return mapToResponse(model);
    }
    
    /**
     * Create new model
     */
    @Transactional
    public DeviceModelResponse createModel(DeviceModelRequest request) {
        log.info("Creating new model: {}", request.getModelName());
        
        // Validate category exists
        DeviceCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException(request.getCategoryId()));
        
        // Validate brand exists
        DeviceBrand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new BrandNotFoundException(request.getBrandId()));
        
        DeviceModel model = DeviceModel.builder()
                .modelName(request.getModelName())
                .category(category)
                .brand(brand)
                .releaseYear(request.getReleaseYear())
                .avgWeightGrams(request.getAvgWeightGrams())
                .goldMg(request.getGoldMg())
                .silverMg(request.getSilverMg())
                .copperG(request.getCopperG())
                .palladiumMg(request.getPalladiumMg())
                .recyclabilityScore(request.getRecyclabilityScore())
                .basePoints(request.getBasePoints())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();
        
        DeviceModel saved = modelRepository.save(model);
        log.info("Model created successfully with id: {}", saved.getId());
        
        return mapToResponse(saved);
    }
    
    /**
     * Update existing model
     */
    @Transactional
    public DeviceModelResponse updateModel(UUID id, DeviceModelRequest request) {
        log.info("Updating model with id: {}", id);
        
        DeviceModel model = modelRepository.findById(id)
                .orElseThrow(() -> new ModelNotFoundException(id));
        
        // Validate and update category if changed
        if (!model.getCategory().getId().equals(request.getCategoryId())) {
            DeviceCategory category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new CategoryNotFoundException(request.getCategoryId()));
            model.setCategory(category);
        }
        
        // Validate and update brand if changed
        if (!model.getBrand().getId().equals(request.getBrandId())) {
            DeviceBrand brand = brandRepository.findById(request.getBrandId())
                    .orElseThrow(() -> new BrandNotFoundException(request.getBrandId()));
            model.setBrand(brand);
        }
        
        model.setModelName(request.getModelName());
        model.setReleaseYear(request.getReleaseYear());
        model.setAvgWeightGrams(request.getAvgWeightGrams());
        model.setGoldMg(request.getGoldMg());
        model.setSilverMg(request.getSilverMg());
        model.setCopperG(request.getCopperG());
        model.setPalladiumMg(request.getPalladiumMg());
        model.setRecyclabilityScore(request.getRecyclabilityScore());
        model.setBasePoints(request.getBasePoints());
        model.setIsActive(request.getIsActive() != null ? request.getIsActive() : model.getIsActive());
        
        DeviceModel updated = modelRepository.save(model);
        log.info("Model updated successfully");
        
        return mapToResponse(updated);
    }
    
    /**
     * Soft delete model (set isActive to false)
     */
    @Transactional
    public void deleteModel(UUID id) {
        log.info("Deleting model with id: {}", id);
        
        DeviceModel model = modelRepository.findById(id)
                .orElseThrow(() -> new ModelNotFoundException(id));
        
        model.setIsActive(false);
        modelRepository.save(model);
        
        log.info("Model soft deleted successfully");
    }
    
    /**
     * Hard delete model (permanent deletion)
     */
    @Transactional
    public void hardDeleteModel(UUID id) {
        log.info("Hard deleting model with id: {}", id);
        
        if (!modelRepository.existsById(id)) {
            throw new ModelNotFoundException(id);
        }
        
        modelRepository.deleteById(id);
        log.info("Model hard deleted successfully");
    }
    
    /**
     * Search models
     */
    @Transactional(readOnly = true)
    public List<DeviceModelResponse> searchModels(String search) {
        log.info("Searching models with term: {}", search);
        
        if (search == null || search.trim().isEmpty()) {
            return getAllModels(null, null, null, null);
        }
        
        List<DeviceModel> models = modelRepository.searchModels(search);
        return models.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Map entity to response DTO
     */
    private DeviceModelResponse mapToResponse(DeviceModel model) {
        return DeviceModelResponse.builder()
                .id(model.getId())
                .modelName(model.getModelName())
                .categoryId(model.getCategory().getId())
                .categoryName(model.getCategory().getName())
                .categoryCode(model.getCategory().getCode())
                .brandId(model.getBrand().getId())
                .brandName(model.getBrand().getName())
                .brandCode(model.getBrand().getCode())
                .releaseYear(model.getReleaseYear())
                .avgWeightGrams(model.getAvgWeightGrams())
                .goldMg(model.getGoldMg())
                .silverMg(model.getSilverMg())
                .copperG(model.getCopperG())
                .palladiumMg(model.getPalladiumMg())
                .recyclabilityScore(model.getRecyclabilityScore())
                .basePoints(model.getBasePoints())
                .isActive(model.getIsActive())
                .createdAt(model.getCreatedAt())
                .updatedAt(model.getUpdatedAt())
                .build();
    }
}
