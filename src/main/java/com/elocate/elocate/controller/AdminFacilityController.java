package com.elocate.elocate.controller;

import com.elocate.elocate.dto.FacilityResponse;
import com.elocate.elocate.model.RecyclingFacility;
import com.elocate.elocate.service.AdminManagementService;
import com.elocate.elocate.service.FacilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * Admin controller for facility management
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/facilities")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminFacilityController {

    private final AdminManagementService adminManagementService;
    private final FacilityService facilityService;

    /**
     * Get all facilities with optional filters
     */
    @GetMapping
    public ResponseEntity<Page<FacilityResponse>> getAllFacilities(
            @RequestParam(required = false) Boolean isVerified,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("Admin fetching facilities - verified: {}, active: {}, search: {}",
                isVerified, isActive, search);

        Page<RecyclingFacility> facilities = facilityService.listFacilities(page, size, search);
        Page<FacilityResponse> response = facilities.map(f -> FacilityResponse.builder()
                .id(f.getId())
                .name(f.getName())
                .address(f.getAddress())
                .contact(f.getUser() != null ? f.getUser().getMobileNumber() : null)
                .time(f.getOperatingHours())
                .verified(Boolean.TRUE.equals(f.getIsVerified()))
                .build());

        return ResponseEntity.ok(response);
    }

    /**
     * Get facility by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<FacilityResponse> getFacilityById(@PathVariable UUID id) {
        log.info("Admin fetching facility: {}", id);
        // FacilityResponse facility = facilityService.getFacilityById(id);
        return ResponseEntity.ok(null);
    }

    /**
     * Approve facility
     */
    @PatchMapping("/{id}/approve")
    public ResponseEntity<Map<String, String>> approveFacility(
            @PathVariable UUID id,
            @RequestBody(required = false) Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Admin approving facility: {}", id);

        UUID adminUserId = UUID.fromString(userDetails.getUsername());
        String notes = body != null ? body.get("notes") : null;

        adminManagementService.approveFacility(id, adminUserId, notes);

        return ResponseEntity.ok(Map.of(
                "message", "Facility approved successfully",
                "facilityId", id.toString()));
    }

    /**
     * Reject facility
     */
    @PatchMapping("/{id}/reject")
    public ResponseEntity<Map<String, String>> rejectFacility(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Admin rejecting facility: {}", id);

        UUID adminUserId = UUID.fromString(userDetails.getUsername());
        String reason = body.get("reason");

        if (reason == null || reason.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Reason is required"));
        }

        adminManagementService.rejectFacility(id, adminUserId, reason);

        return ResponseEntity.ok(Map.of(
                "message", "Facility rejected successfully",
                "facilityId", id.toString()));
    }
}
