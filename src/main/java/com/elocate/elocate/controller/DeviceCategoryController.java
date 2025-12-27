package com.elocate.elocate.controller;

import com.elocate.elocate.dto.DeviceCategoryRequest;
import com.elocate.elocate.dto.DeviceCategoryResponse;
import com.elocate.elocate.service.DeviceCategoryService;
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
@RequestMapping("/api/v1/device-categories")
@RequiredArgsConstructor
public class DeviceCategoryController {
    
    private final DeviceCategoryService categoryService;
    
    /**
     * Get all categories with optional search and filter
     * Query params:
     * - search: Search term for name, code, or description
     * - isActive: Filter by active status (true/false)
     */
    @GetMapping
    public ResponseEntity<List<DeviceCategoryResponse>> getAllCategories(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive) {
        log.info("GET /api/v1/device-categories - search: {}, isActive: {}", search, isActive);
        List<DeviceCategoryResponse> categories = categoryService.getAllCategories(search, isActive);
        return ResponseEntity.ok(categories);
    }
    
    /**
     * Get category by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<DeviceCategoryResponse> getCategoryById(@PathVariable UUID id) {
        log.info("GET /api/v1/device-categories/{}", id);
        DeviceCategoryResponse category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }
    
    /**
     * Create new category
     */
    @PostMapping
    public ResponseEntity<DeviceCategoryResponse> createCategory(
            @Valid @RequestBody DeviceCategoryRequest request) {
        log.info("POST /api/v1/device-categories - code: {}", request.getCode());
        DeviceCategoryResponse created = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    /**
     * Update existing category
     */
    @PutMapping("/{id}")
    public ResponseEntity<DeviceCategoryResponse> updateCategory(
            @PathVariable UUID id,
            @Valid @RequestBody DeviceCategoryRequest request) {
        log.info("PUT /api/v1/device-categories/{} - code: {}", id, request.getCode());
        DeviceCategoryResponse updated = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(updated);
    }
    
    /**
     * Soft delete category (set isActive to false)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable UUID id) {
        log.info("DELETE /api/v1/device-categories/{}", id);
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Hard delete category (permanent deletion)
     */
    @DeleteMapping("/{id}/hard")
    public ResponseEntity<Void> hardDeleteCategory(@PathVariable UUID id) {
        log.info("DELETE /api/v1/device-categories/{}/hard", id);
        categoryService.hardDeleteCategory(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Search categories
     */
    @GetMapping("/search")
    public ResponseEntity<List<DeviceCategoryResponse>> searchCategories(
            @RequestParam String q) {
        log.info("GET /api/v1/device-categories/search?q={}", q);
        List<DeviceCategoryResponse> results = categoryService.searchCategories(q);
        return ResponseEntity.ok(results);
    }
}
