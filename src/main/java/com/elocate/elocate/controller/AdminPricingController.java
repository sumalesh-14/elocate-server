package com.elocate.elocate.controller;

import com.elocate.elocate.dto.*;
import com.elocate.elocate.model.DeviceModel;
import com.elocate.elocate.model.MetalRate;
import com.elocate.elocate.service.AdminManagementService;
import com.elocate.elocate.service.EnhancedPricingService;
import com.elocate.elocate.service.MetalRateService;
import com.elocate.elocate.repository.DeviceModelRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Admin controller for pricing management
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/pricing")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminPricingController {

    private final AdminManagementService adminManagementService;
    private final MetalRateService metalRateService;
    private final DeviceModelRepository deviceModelRepository;
    private final EnhancedPricingService pricingService;

    /**
     * Get all metal rates
     */
    @GetMapping("/metal-rates")
    public ResponseEntity<List<MetalRateResponse>> getAllMetalRates() {
        log.info("Admin fetching all metal rates");
        List<MetalRateResponse> rates = metalRateService.getAllMetalRates(null, null);
        return ResponseEntity.ok(rates);
    }

    /**
     * Create or update metal rate
     */
    @PostMapping("/metal-rates")
    public ResponseEntity<MetalRateResponse> createMetalRate(
            @Valid @RequestBody MetalRateRequest request) {

        log.info("Admin creating metal rate for: {}", request.getMetalType());
        MetalRateResponse rate = metalRateService.createMetalRate(request);
        return ResponseEntity.ok(rate);
    }

    /**
     * Update metal rate
     */
    @PutMapping("/metal-rates/{id}")
    public ResponseEntity<MetalRateResponse> updateMetalRate(
            @PathVariable UUID id,
            @Valid @RequestBody MetalRateRequest request) {

        log.info("Admin updating metal rate: {}", id);
        MetalRateResponse rate = metalRateService.updateMetalRate(id, request);
        return ResponseEntity.ok(rate);
    }

    /**
     * Get AI pricing for a device model
     */
    @GetMapping("/models/{id}/ai-pricing")
    public ResponseEntity<PricingBreakdownResponse> getAIPricing(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "GOOD") String conditionCode) {

        log.info("Admin fetching AI pricing for model: {}", id);

        DeviceModel model = deviceModelRepository.findById(id).orElseThrow();
        PricingBreakdownResponse breakdown = pricingService.calculatePricingBreakdown(
                model, conditionCode, LocalDate.now());

        return ResponseEntity.ok(breakdown);
    }

    /**
     * Override device model price
     */
    @PostMapping("/models/{id}/price-override")
    public ResponseEntity<Map<String, String>> overrideModelPrice(
            @PathVariable UUID id,
            @Valid @RequestBody AdminPriceOverrideRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Admin overriding price for model: {}", id);

        UUID adminUserId = UUID.fromString(userDetails.getUsername());
        adminManagementService.overrideModelPrice(id, request, adminUserId);

        return ResponseEntity.ok(Map.of(
                "message", "Price override applied successfully",
                "modelId", id.toString(),
                "overridePrice", request.getOverridePrice().toString()));
    }

    /**
     * Remove price override
     */
    @DeleteMapping("/models/{id}/price-override")
    public ResponseEntity<Map<String, String>> removePriceOverride(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Admin removing price override for model: {}", id);

        DeviceModel model = deviceModelRepository.findById(id).orElseThrow();
        model.setManualPriceOverride(null);
        model.setPriceOverrideReason(null);
        model.setPriceOverrideBy(null);
        model.setPriceOverrideAt(null);
        deviceModelRepository.save(model);

        return ResponseEntity.ok(Map.of(
                "message", "Price override removed successfully",
                "modelId", id.toString()));
    }
}
