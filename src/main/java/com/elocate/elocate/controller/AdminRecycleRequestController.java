package com.elocate.elocate.controller;

import com.elocate.elocate.dto.AdminPriceChangeRequest;
import com.elocate.elocate.dto.FacilityReassignmentRequest;
import com.elocate.elocate.dto.RecycleRequestResponse;
import com.elocate.elocate.model.RecycleStatus;
import com.elocate.elocate.service.AdminManagementService;
import com.elocate.elocate.service.RecycleRequestService;
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

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Admin controller for recycle request management
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/recycle-requests")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminRecycleRequestController {

    private final AdminManagementService adminManagementService;
    private final RecycleRequestService recycleRequestService;

    /**
     * Get all recycle requests with filters
     */
    @GetMapping
    public ResponseEntity<List<RecycleRequestResponse>> getAllRecycleRequests(
            @RequestParam(required = false) RecycleStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID facilityId) {

        log.info("Admin fetching recycle requests - status: {}, search: {}, facility: {}",
                status, search, facilityId);

        // For now, return all requests - can be enhanced with admin-specific repository
        // method
        // List<RecycleRequestResponse> requests =
        // recycleRequestService.getAllRecycleRequests(
        // status, search);

        return ResponseEntity.ok(List.of());
    }

    /**
     * Get recycle request by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<RecycleRequestResponse> getRecycleRequestById(@PathVariable UUID id) {
        log.info("Admin fetching recycle request: {}", id);
        RecycleRequestResponse request = recycleRequestService.getRecycleRequestById(id);
        return ResponseEntity.ok(request);
    }

    /**
     * Reassign request to different facility
     */
    @PostMapping("/{id}/reassign-facility")
    public ResponseEntity<Map<String, String>> reassignFacility(
            @PathVariable UUID id,
            @Valid @RequestBody FacilityReassignmentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Admin reassigning request {} to facility {}", id, request.getNewFacilityId());

        UUID adminUserId = UUID.fromString(userDetails.getUsername());
        adminManagementService.reassignFacility(id, request, adminUserId);

        return ResponseEntity.ok(Map.of(
                "message", "Facility reassigned successfully",
                "requestId", id.toString(),
                "newFacilityId", request.getNewFacilityId().toString()));
    }

    /**
     * Update pricing for recycle request
     */
    @PostMapping("/{id}/update-pricing")
    public ResponseEntity<Map<String, String>> updatePricing(
            @PathVariable UUID id,
            @Valid @RequestBody AdminPriceChangeRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Admin updating pricing for request: {}", id);

        UUID adminUserId = UUID.fromString(userDetails.getUsername());
        adminManagementService.changePricing(id, request, adminUserId);

        return ResponseEntity.ok(Map.of(
                "message", "Pricing updated successfully",
                "requestId", id.toString()));
    }
}
