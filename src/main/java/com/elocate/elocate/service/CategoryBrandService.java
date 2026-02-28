package com.elocate.elocate.service;

import com.elocate.elocate.dto.CategoryBrandRequest;
import com.elocate.elocate.dto.CategoryBrandResponse;
import com.elocate.elocate.dto.DeviceBrandResponse;
import com.elocate.elocate.dto.DeviceCategoryResponse;
import com.elocate.elocate.exception.ResourceNotFoundException;
import com.elocate.elocate.model.CategoryBrand;
import com.elocate.elocate.model.DeviceBrand;
import com.elocate.elocate.model.DeviceCategory;
import com.elocate.elocate.repository.CategoryBrandRepository;
import com.elocate.elocate.repository.DeviceBrandRepository;
import com.elocate.elocate.repository.DeviceCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryBrandService {

    private final CategoryBrandRepository categoryBrandRepository;
    private final DeviceCategoryRepository categoryRepository;
    private final DeviceBrandRepository brandRepository;

    @Transactional
    public CategoryBrandResponse createCategoryBrand(CategoryBrandRequest request) {
        log.info("Creating CategoryBrand link - categoryId: {}, brandId: {}", request.getCategoryId(),
                request.getBrandId());

        if (categoryBrandRepository.existsByCategoryIdAndBrandId(request.getCategoryId(), request.getBrandId())) {
            throw new IllegalArgumentException("Link between this Category and Brand already exists");
        }

        DeviceCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Device Category not found"));

        DeviceBrand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new ResourceNotFoundException("Device Brand not found"));

        CategoryBrand categoryBrand = CategoryBrand.builder()
                .category(category)
                .brand(brand)
                .isActive(request.getIsActive())
                .build();

        CategoryBrand saved = categoryBrandRepository.save(categoryBrand);
        return mapToResponse(saved);
    }

    public Page<CategoryBrandResponse> getAllCategoryBrands(Pageable pageable) {
        return categoryBrandRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    public Page<CategoryBrandResponse> getBrandsByCategory(UUID categoryId, Pageable pageable) {
        return categoryBrandRepository.findByCategoryId(categoryId, pageable)
                .map(this::mapToResponse);
    }

    public Page<CategoryBrandResponse> getCategoriesByBrand(UUID brandId, Pageable pageable) {
        return categoryBrandRepository.findByBrandId(brandId, pageable)
                .map(this::mapToResponse);
    }

    @Transactional
    public void deleteCategoryBrand(UUID id) {
        if (!categoryBrandRepository.existsById(id)) {
            throw new ResourceNotFoundException("CategoryBrand link not found");
        }
        categoryBrandRepository.deleteById(id);
    }

    private CategoryBrandResponse mapToResponse(CategoryBrand entity) {
        return CategoryBrandResponse.builder()
                .id(entity.getId())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .category(DeviceCategoryResponse.builder()
                        .id(entity.getCategory().getId())
                        .code(entity.getCategory().getCode())
                        .name(entity.getCategory().getName())
                        .description(entity.getCategory().getDescription())
                        .isActive(entity.getCategory().getIsActive())
                        .createdAt(entity.getCategory().getCreatedAt())
                        .updatedAt(entity.getCategory().getUpdatedAt())
                        .build())
                .brand(DeviceBrandResponse.builder()
                        .id(entity.getBrand().getId())
                        .code(entity.getBrand().getCode())
                        .name(entity.getBrand().getName())
                        .description(entity.getBrand().getDescription())
                        .isActive(entity.getBrand().getIsActive())
                        .createdAt(entity.getBrand().getCreatedAt())
                        .updatedAt(entity.getBrand().getUpdatedAt())
                        .build())
                .build();
    }
}
