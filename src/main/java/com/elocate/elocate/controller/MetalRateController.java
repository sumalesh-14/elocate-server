package com.elocate.elocate.controller;

import com.elocate.elocate.dto.MetalRateRequest;
import com.elocate.elocate.dto.MetalRateResponse;
import com.elocate.elocate.model.MetalType;
import com.elocate.elocate.service.MetalRateService;
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
@RequestMapping("/api/v1/metal-rates")
@RequiredArgsConstructor
public class MetalRateController {
    
    private final MetalRateService metalRateService;
    
    /**
     * Get all metal rates with optional filters
     * Query params:
     * - metalType: Filter by metal type (GOLD, SILVER, COPPER, PALLADIUM)
     * - isActive: Filter by active status (true/false)
     */
    @GetMapping
    public ResponseEntity<List<MetalRateResponse>> getAllMetalRates(
            @RequestParam(required = false) MetalType metalType,
            @RequestParam(required = false) Boolean isActive) {
        log.info("GET /api/v1/metal-rates - metalType: {}, isActive: {}", metalType, isActive);
        List<MetalRateResponse> rates = metalRateService.getAllMetalRates(metalType, isActive);
        return ResponseEntity.ok(rates);
    }
    
    /**
     * Get metal rate by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<MetalRateResponse> getMetalRateById(@PathVariable UUID id) {
        log.info("GET /api/v1/metal-rates/{}", id);
        MetalRateResponse rate = metalRateService.getMetalRateById(id);
        return ResponseEntity.ok(rate);
    }
    
    /**
     * Create new metal rate
     */
    @PostMapping
    public ResponseEntity<MetalRateResponse> createMetalRate(
            @Valid @RequestBody MetalRateRequest request) {
        log.info("POST /api/v1/metal-rates - metalType: {}", request.getMetalType());
        MetalRateResponse created = metalRateService.createMetalRate(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    /**
     * Update existing metal rate
     */
    @PutMapping("/{id}")
    public ResponseEntity<MetalRateResponse> updateMetalRate(
            @PathVariable UUID id,
            @Valid @RequestBody MetalRateRequest request) {
        log.info("PUT /api/v1/metal-rates/{} - metalType: {}", id, request.getMetalType());
        MetalRateResponse updated = metalRateService.updateMetalRate(id, request);
        return ResponseEntity.ok(updated);
    }
}
