package com.elocate.elocate.controller;

import com.elocate.elocate.dto.CategoryBrandRequest;
import com.elocate.elocate.dto.CategoryBrandResponse;
import com.elocate.elocate.service.CategoryBrandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/category-brands")
@RequiredArgsConstructor
public class CategoryBrandController {

    private final CategoryBrandService categoryBrandService;

    @PostMapping
    public ResponseEntity<CategoryBrandResponse> createCategoryBrand(
            @Valid @RequestBody CategoryBrandRequest request) {
        log.info("POST /api/v1/category-brands");
        CategoryBrandResponse created = categoryBrandService.createCategoryBrand(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<Page<CategoryBrandResponse>> getAllCategoryBrands(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /api/v1/category-brands - page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(categoryBrandService.getAllCategoryBrands(pageable));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<CategoryBrandResponse>> getBrandsByCategory(
            @PathVariable UUID categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /api/v1/category-brands/category/{} - page: {}, size: {}", categoryId, page, size);
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(categoryBrandService.getBrandsByCategory(categoryId, pageable));
    }

    @GetMapping("/brand/{brandId}")
    public ResponseEntity<Page<CategoryBrandResponse>> getCategoriesByBrand(
            @PathVariable UUID brandId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /api/v1/category-brands/brand/{} - page: {}, size: {}", brandId, page, size);
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(categoryBrandService.getCategoriesByBrand(brandId, pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategoryBrand(@PathVariable UUID id) {
        log.info("DELETE /api/v1/category-brands/{}", id);
        categoryBrandService.deleteCategoryBrand(id);
        return ResponseEntity.noContent().build();
    }
}
