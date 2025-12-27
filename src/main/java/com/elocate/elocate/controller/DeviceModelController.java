package com.elocate.elocate.controller;

import com.elocate.elocate.dto.DeviceModelRequest;
import com.elocate.elocate.dto.DeviceModelResponse;
import com.elocate.elocate.service.DeviceModelService;
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
    public ResponseEntity<List<DeviceModelResponse>> getAllModels(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) UUID brandId) {
        log.info("GET /api/v1/device-models - search: {}, isActive: {}, categoryId: {}, brandId: {}", 
                search, isActive, categoryId, brandId);
        List<DeviceModelResponse> models = modelService.getAllModels(search, isActive, categoryId, brandId);
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
    public ResponseEntity<List<DeviceModelResponse>> searchModels(
            @RequestParam String q) {
        log.info("GET /api/v1/device-models/search?q={}", q);
        List<DeviceModelResponse> results = modelService.searchModels(q);
        return ResponseEntity.ok(results);
    }
}
