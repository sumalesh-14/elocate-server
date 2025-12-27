package com.elocate.elocate.service;

import com.elocate.elocate.dto.DeviceBrandRequest;
import com.elocate.elocate.dto.DeviceBrandResponse;
import com.elocate.elocate.exception.BrandNotFoundException;
import com.elocate.elocate.exception.DuplicateBrandCodeException;
import com.elocate.elocate.model.DeviceBrand;
import com.elocate.elocate.repository.DeviceBrandRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class DeviceBrandService {
    
    private final DeviceBrandRepository brandRepository;
    
    /**
     * Get all brands with optional search and filter
     */
    @Transactional(readOnly = true)
    public List<DeviceBrandResponse> getAllBrands(String search, Boolean isActive) {
        log.info("Fetching brands with search: {}, isActive: {}", search, isActive);
        
        List<DeviceBrand> brands;
        
        if (search != null && !search.trim().isEmpty()) {
            if (isActive != null) {
                brands = brandRepository.searchBrandsWithActiveFilter(search, isActive);
            } else {
                brands = brandRepository.searchBrands(search);
            }
        } else if (isActive != null) {
            brands = brandRepository.findByIsActiveOrderByNameAsc(isActive);
        } else {
            brands = brandRepository.findAll();
        }
        
        return brands.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get brand by ID
     */
    @Transactional(readOnly = true)
    public DeviceBrandResponse getBrandById(UUID id) {
        log.info("Fetching brand by id: {}", id);
        DeviceBrand brand = brandRepository.findById(id)
                .orElseThrow(() -> new BrandNotFoundException(id));
        return mapToResponse(brand);
    }
    
    /**
     * Create new brand
     */
    @Transactional
    public DeviceBrandResponse createBrand(DeviceBrandRequest request) {
        log.info("Creating new brand with code: {}", request.getCode());
        
        // Check if code already exists
        Optional<DeviceBrand> existing = brandRepository.findByCodeIgnoreCase(request.getCode());
        if (existing.isPresent()) {
            throw new DuplicateBrandCodeException(request.getCode());
        }
        
        DeviceBrand brand = DeviceBrand.builder()
                .code(request.getCode().toUpperCase())
                .name(request.getName())
                .description(request.getDescription())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();
        
        DeviceBrand saved = brandRepository.save(brand);
        log.info("Brand created successfully with id: {}", saved.getId());
        
        return mapToResponse(saved);
    }
    
    /**
     * Update existing brand
     */
    @Transactional
    public DeviceBrandResponse updateBrand(UUID id, DeviceBrandRequest request) {
        log.info("Updating brand with id: {}", id);
        
        DeviceBrand brand = brandRepository.findById(id)
                .orElseThrow(() -> new BrandNotFoundException(id));
        
        // Check if code is being changed and if new code already exists
        if (!brand.getCode().equalsIgnoreCase(request.getCode())) {
            Optional<DeviceBrand> existing = brandRepository.findByCodeIgnoreCase(request.getCode());
            if (existing.isPresent() && !existing.get().getId().equals(id)) {
                throw new DuplicateBrandCodeException(request.getCode());
            }
            brand.setCode(request.getCode().toUpperCase());
        }
        
        brand.setName(request.getName());
        brand.setDescription(request.getDescription());
        brand.setIsActive(request.getIsActive() != null ? request.getIsActive() : brand.getIsActive());
        
        DeviceBrand updated = brandRepository.save(brand);
        log.info("Brand updated successfully");
        
        return mapToResponse(updated);
    }
    
    /**
     * Soft delete brand (set isActive to false)
     */
    @Transactional
    public void deleteBrand(UUID id) {
        log.info("Deleting brand with id: {}", id);
        
        DeviceBrand brand = brandRepository.findById(id)
                .orElseThrow(() -> new BrandNotFoundException(id));
        
        brand.setIsActive(false);
        brandRepository.save(brand);
        
        log.info("Brand soft deleted successfully");
    }
    
    /**
     * Hard delete brand (permanent deletion)
     */
    @Transactional
    public void hardDeleteBrand(UUID id) {
        log.info("Hard deleting brand with id: {}", id);
        
        if (!brandRepository.existsById(id)) {
            throw new BrandNotFoundException(id);
        }
        
        brandRepository.deleteById(id);
        log.info("Brand hard deleted successfully");
    }
    
    /**
     * Search brands
     */
    @Transactional(readOnly = true)
    public List<DeviceBrandResponse> searchBrands(String search) {
        log.info("Searching brands with term: {}", search);
        
        if (search == null || search.trim().isEmpty()) {
            return getAllBrands(null, null);
        }
        
        List<DeviceBrand> brands = brandRepository.searchBrands(search);
        return brands.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Map entity to response DTO
     */
    private DeviceBrandResponse mapToResponse(DeviceBrand brand) {
        return DeviceBrandResponse.builder()
                .id(brand.getId())
                .code(brand.getCode())
                .name(brand.getName())
                .description(brand.getDescription())
                .isActive(brand.getIsActive())
                .createdAt(brand.getCreatedAt())
                .updatedAt(brand.getUpdatedAt())
                .build();
    }
}
