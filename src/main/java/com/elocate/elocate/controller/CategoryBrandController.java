package com.elocate.elocate.controller;

import com.elocate.elocate.dto.CategoryBrandRequest;
import com.elocate.elocate.dto.CategoryBrandResponse;
import com.elocate.elocate.service.CategoryBrandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
    public ResponseEntity<List<CategoryBrandResponse>> getAllCategoryBrands() {
        log.info("GET /api/v1/category-brands");
        return ResponseEntity.ok(categoryBrandService.getAllCategoryBrands());
    }
    
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<CategoryBrandResponse>> getBrandsByCategory(@PathVariable UUID categoryId) {
        log.info("GET /api/v1/category-brands/category/{}", categoryId);
        return ResponseEntity.ok(categoryBrandService.getBrandsByCategory(categoryId));
    }
    
    @GetMapping("/brand/{brandId}")
    public ResponseEntity<List<CategoryBrandResponse>> getCategoriesByBrand(@PathVariable UUID brandId) {
        log.info("GET /api/v1/category-brands/brand/{}", brandId);
        return ResponseEntity.ok(categoryBrandService.getCategoriesByBrand(brandId));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategoryBrand(@PathVariable UUID id) {
        log.info("DELETE /api/v1/category-brands/{}", id);
        categoryBrandService.deleteCategoryBrand(id);
        return ResponseEntity.noContent().build();
    }
}
