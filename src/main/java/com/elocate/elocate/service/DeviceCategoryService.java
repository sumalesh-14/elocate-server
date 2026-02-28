package com.elocate.elocate.service;

import com.elocate.elocate.dto.DeviceCategoryRequest;
import com.elocate.elocate.dto.DeviceCategoryResponse;
import com.elocate.elocate.exception.CategoryNotFoundException;
import com.elocate.elocate.exception.DuplicateCategoryCodeException;
import com.elocate.elocate.model.DeviceCategory;
import com.elocate.elocate.repository.DeviceCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class DeviceCategoryService {

    private final DeviceCategoryRepository categoryRepository;

    /**
     * Get all categories with optional search and filter
     */
    @Transactional(readOnly = true)
    public Page<DeviceCategoryResponse> getAllCategories(String search, Boolean isActive, Pageable pageable) {
        log.info("Fetching categories with search: {}, isActive: {}, pageable: {}", search, isActive, pageable);

        Page<DeviceCategory> categories;

        if (search != null && !search.trim().isEmpty()) {
            if (isActive != null) {
                categories = categoryRepository.searchCategoriesWithActiveFilter(search, isActive, pageable);
            } else {
                categories = categoryRepository.searchCategories(search, pageable);
            }
        } else if (isActive != null) {
            categories = categoryRepository.findByIsActiveOrderByNameAsc(isActive, pageable);
        } else {
            categories = categoryRepository.findAll(pageable);
        }

        return categories.map(this::mapToResponse);
    }

    /**
     * Get category by ID
     */
    @Transactional(readOnly = true)
    public DeviceCategoryResponse getCategoryById(UUID id) {
        log.info("Fetching category by id: {}", id);
        DeviceCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
        return mapToResponse(category);
    }

    /**
     * Create new category
     */
    @Transactional
    public DeviceCategoryResponse createCategory(DeviceCategoryRequest request) {
        log.info("Creating new category with code: {}", request.getCode());

        // Check if code already exists
        Optional<DeviceCategory> existing = categoryRepository.findByCodeIgnoreCase(request.getCode());
        if (existing.isPresent()) {
            throw new DuplicateCategoryCodeException(request.getCode());
        }

        DeviceCategory category = DeviceCategory.builder()
                .code(request.getCode().toUpperCase())
                .name(request.getName())
                .description(request.getDescription())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        DeviceCategory saved = categoryRepository.save(category);
        log.info("Category created successfully with id: {}", saved.getId());

        return mapToResponse(saved);
    }

    /**
     * Update existing category
     */
    @Transactional
    public DeviceCategoryResponse updateCategory(UUID id, DeviceCategoryRequest request) {
        log.info("Updating category with id: {}", id);

        DeviceCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));

        // Check if code is being changed and if new code already exists
        if (!category.getCode().equalsIgnoreCase(request.getCode())) {
            Optional<DeviceCategory> existing = categoryRepository.findByCodeIgnoreCase(request.getCode());
            if (existing.isPresent() && !existing.get().getId().equals(id)) {
                throw new DuplicateCategoryCodeException(request.getCode());
            }
            category.setCode(request.getCode().toUpperCase());
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setIsActive(request.getIsActive() != null ? request.getIsActive() : category.getIsActive());

        DeviceCategory updated = categoryRepository.save(category);
        log.info("Category updated successfully");

        return mapToResponse(updated);
    }

    /**
     * Soft delete category (set isActive to false)
     */
    @Transactional
    public void deleteCategory(UUID id) {
        log.info("Deleting category with id: {}", id);

        DeviceCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));

        category.setIsActive(false);
        categoryRepository.save(category);

        log.info("Category soft deleted successfully");
    }

    /**
     * Hard delete category (permanent deletion)
     */
    @Transactional
    public void hardDeleteCategory(UUID id) {
        log.info("Hard deleting category with id: {}", id);

        if (!categoryRepository.existsById(id)) {
            throw new CategoryNotFoundException(id);
        }

        categoryRepository.deleteById(id);
        log.info("Category hard deleted successfully");
    }

    /**
     * Search categories
     */
    @Transactional(readOnly = true)
    public Page<DeviceCategoryResponse> searchCategories(String search, Pageable pageable) {
        log.info("Searching categories with term: {}, pageable: {}", search, pageable);

        if (search == null || search.trim().isEmpty()) {
            return getAllCategories(null, null, pageable);
        }

        Page<DeviceCategory> categories = categoryRepository.searchCategories(search, pageable);
        return categories.map(this::mapToResponse);
    }

    /**
     * Map entity to response DTO
     */
    private DeviceCategoryResponse mapToResponse(DeviceCategory category) {
        return DeviceCategoryResponse.builder()
                .id(category.getId())
                .code(category.getCode())
                .name(category.getName())
                .description(category.getDescription())
                .isActive(category.getIsActive())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}
