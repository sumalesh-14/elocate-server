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
    private final com.elocate.elocate.security.JwtUtil jwtUtil;

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

        List<RecycleRequestResponse> requests = recycleRequestService.getAllRecycleRequests(
                status, search);

        return ResponseEntity.ok(requests);
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
            jakarta.servlet.http.HttpServletRequest httpRequest) {

        log.info("Admin reassigning request {} to facility {}", id, request.getNewFacilityId());

        // Extract JWT token from Authorization header
        String authHeader = httpRequest.getHeader("Authorization");
        UUID adminUserId = null;
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                adminUserId = jwtUtil.extractUserId(token);
            } catch (Exception e) {
                log.error("Failed to extract user ID from token: {}", e.getMessage());
                throw new IllegalArgumentException("Unable to determine admin user ID from token");
            }
        }
        
        if (adminUserId == null) {
            throw new IllegalArgumentException("Unable to determine admin user ID");
        }
        
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
