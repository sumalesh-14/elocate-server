package com.elocate.elocate.controller;

import com.elocate.elocate.dto.DeviceBrandRequest;
import com.elocate.elocate.dto.DeviceBrandResponse;
import com.elocate.elocate.service.DeviceBrandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/device-brands")
@RequiredArgsConstructor
public class DeviceBrandController {

    private final DeviceBrandService brandService;

    /**
     * Get all brands with optional search and filter
     * Query params:
     * - search: Search term for name, code, or description
     * - isActive: Filter by active status (true/false)
     * - page: Page number (default: 0)
     * - size: Page size (default: 10)
     */
    @GetMapping
    public ResponseEntity<Page<DeviceBrandResponse>> getAllBrands(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /api/v1/device-brands - search: {}, isActive: {}, page: {}, size: {}", search, isActive, page,
                size);
        Pageable pageable = PageRequest.of(page, size);
        Page<DeviceBrandResponse> brands = brandService.getAllBrands(search, isActive, pageable);
        return ResponseEntity.ok(brands);
    }

    /**
     * Get brand by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<DeviceBrandResponse> getBrandById(@PathVariable UUID id) {
        log.info("GET /api/v1/device-brands/{}", id);
        DeviceBrandResponse brand = brandService.getBrandById(id);
        return ResponseEntity.ok(brand);
    }

    /**
     * Create new brand
     */
    @PostMapping
    public ResponseEntity<DeviceBrandResponse> createBrand(
            @Valid @RequestBody DeviceBrandRequest request) {
        log.info("POST /api/v1/device-brands - code: {}", request.getCode());
        DeviceBrandResponse created = brandService.createBrand(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Update existing brand
     */
    @PutMapping("/{id}")
    public ResponseEntity<DeviceBrandResponse> updateBrand(
            @PathVariable UUID id,
            @Valid @RequestBody DeviceBrandRequest request) {
        log.info("PUT /api/v1/device-brands/{} - code: {}", id, request.getCode());
        DeviceBrandResponse updated = brandService.updateBrand(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * Soft delete brand (set isActive to false)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBrand(@PathVariable UUID id) {
        log.info("DELETE /api/v1/device-brands/{}", id);
        brandService.deleteBrand(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Hard delete brand (permanent deletion)
     */
    @DeleteMapping("/{id}/hard")
    public ResponseEntity<Void> hardDeleteBrand(@PathVariable UUID id) {
        log.info("DELETE /api/v1/device-brands/{}/hard", id);
        brandService.hardDeleteBrand(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Search brands
     */
    @GetMapping("/search")
    public ResponseEntity<Page<DeviceBrandResponse>> searchBrands(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /api/v1/device-brands/search?q={}&page={}&size={}", q, page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<DeviceBrandResponse> results = brandService.searchBrands(q, pageable);
        return ResponseEntity.ok(results);
    }
}
