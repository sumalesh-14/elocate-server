package com.elocate.elocate.controller;

import com.elocate.elocate.dto.DeviceModelRequest;
import com.elocate.elocate.dto.DeviceModelResponse;
import com.elocate.elocate.service.DeviceModelService;
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
@RequestMapping("/api/v1/device-models")
@RequiredArgsConstructor
public class DeviceModelController {

    private final DeviceModelService modelService;

    /**
     * Get all models with optional search and filters
     * Query params:
     * - search: Search term for model name
     * - isActive: Filter by active status (true/false)
     * - categoryId: Filter by category UUID
     * - brandId: Filter by brand UUID
     */
    @GetMapping
    public ResponseEntity<Page<DeviceModelResponse>> getAllModels(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) UUID brandId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info(
                "GET /api/v1/device-models - search: {}, isActive: {}, categoryId: {}, brandId: {}, page: {}, size: {}",
                search, isActive, categoryId, brandId, page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<DeviceModelResponse> models = modelService.getAllModels(search, isActive, categoryId, brandId, pageable);
        return ResponseEntity.ok(models);
    }

    /**
     * Get model by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<DeviceModelResponse> getModelById(@PathVariable UUID id) {
        log.info("GET /api/v1/device-models/{}", id);
        DeviceModelResponse model = modelService.getModelById(id);
        return ResponseEntity.ok(model);
    }

    /**
     * Create new model
     */
    @PostMapping
    public ResponseEntity<DeviceModelResponse> createModel(
            @Valid @RequestBody DeviceModelRequest request) {
        log.info("POST /api/v1/device-models - modelName: {}", request.getModelName());
        DeviceModelResponse created = modelService.createModel(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Update existing model
     */
    @PutMapping("/{id}")
    public ResponseEntity<DeviceModelResponse> updateModel(
            @PathVariable UUID id,
            @Valid @RequestBody DeviceModelRequest request) {
        log.info("PUT /api/v1/device-models/{} - modelName: {}", id, request.getModelName());
        DeviceModelResponse updated = modelService.updateModel(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * Soft delete model (set isActive to false)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteModel(@PathVariable UUID id) {
        log.info("DELETE /api/v1/device-models/{}", id);
        modelService.deleteModel(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Hard delete model (permanent deletion)
     */
    @DeleteMapping("/{id}/hard")
    public ResponseEntity<Void> hardDeleteModel(@PathVariable UUID id) {
        log.info("DELETE /api/v1/device-models/{}/hard", id);
        modelService.hardDeleteModel(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Search models
     */
    @GetMapping("/search")
    public ResponseEntity<Page<DeviceModelResponse>> searchModels(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("GET /api/v1/device-models/search?q={} - page: {}, size: {}", q, page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<DeviceModelResponse> results = modelService.searchModels(q, pageable);
        return ResponseEntity.ok(results);
    }
}
