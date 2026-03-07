package com.elocate.elocate.controller;

import com.elocate.elocate.dto.CreateRecycleRequestDto;
import com.elocate.elocate.dto.RecycleRequestResponse;
import com.elocate.elocate.dto.UpdateFulfillmentStatusDto;
import com.elocate.elocate.dto.VerifyRecycleRequestDto;
import com.elocate.elocate.dto.AssignDriverDto;
import com.elocate.elocate.dto.DriverActionReasonDto;
import com.elocate.elocate.model.RecycleStatus;
import com.elocate.elocate.model.RecycleStatusHistory;
import com.elocate.elocate.service.RecycleRequestService;
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
@RequestMapping("/api/v1/recycle-requests")
@RequiredArgsConstructor
public class RecycleRequestController {

    private final RecycleRequestService recycleRequestService;

    /**
     * Create new recycle request with point estimation and fulfillment tracking
     * 
     * Steps:
     * 1. Validate fulfillment requirements (pickup address or facility)
     * 2. Fetch device model and metal rates
     * 3. Calculate estimated points
     * 4. Initialize fulfillment status based on type
     * 5. Save recycle request
     * 6. Create reward snapshot (freeze calculation)
     * 7. Record initial status in history
     */
    @PostMapping
    public ResponseEntity<RecycleRequestResponse> createRecycleRequest(
            @RequestParam UUID userId,
            @Valid @RequestBody CreateRecycleRequestDto request) {
        log.info("POST /api/v1/recycle-requests - userId: {}, deviceModelId: {}",
                userId, request.getDeviceModelId());

        RecycleRequestResponse response = recycleRequestService.createRecycleRequest(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get recycle requests by user ID with optional filters
     */
    @GetMapping
    public ResponseEntity<List<RecycleRequestResponse>> getRecycleRequests(
            @RequestParam UUID userId,
            @RequestParam(required = false) com.elocate.elocate.model.RecycleStatus status,
            @RequestParam(required = false) String searchTerm) {
        log.info("GET /api/v1/recycle-requests - userId: {}, status: {}, search: {}",
                userId, status, searchTerm);

        List<RecycleRequestResponse> responses = recycleRequestService.getRecycleRequests(userId, status, searchTerm);
        return ResponseEntity.ok(responses);
    }

    /**
     * Get recycle request by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<RecycleRequestResponse> getRecycleRequestById(@PathVariable UUID id) {
        log.info("GET /api/v1/recycle-requests/{}", id);
        RecycleRequestResponse response = recycleRequestService.getRecycleRequestById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Verify recycle request and recalculate final points
     * 
     * Steps:
     * 1. Fetch reward snapshot (frozen rates)
     * 2. Recalculate points with verified weight/condition
     * 3. Update final points and status
     * 4. Credit user wallet
     */
    @PutMapping("/{id}/verify")
    public ResponseEntity<RecycleRequestResponse> verifyRecycleRequest(
            @PathVariable UUID id,
            @Valid @RequestBody VerifyRecycleRequestDto request) {
        log.info("PUT /api/v1/recycle-requests/{}/verify - weight: {}, condition: {}",
                id, request.getVerifiedWeightGrams(), request.getVerifiedConditionCode());

        RecycleRequestResponse response = recycleRequestService.verifyRecycleRequest(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Update fulfillment status of a recycle request
     * 
     * This endpoint allows updating the logistics status (pickup or drop-off flow)
     * Validates that status transitions are valid (e.g., cannot go from PICKUP to
     * DROP status)
     */
    @PutMapping("/{id}/fulfillment-status")
    public ResponseEntity<RecycleRequestResponse> updateFulfillmentStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateFulfillmentStatusDto request) {
        log.info("PUT /api/v1/recycle-requests/{}/fulfillment-status - new status: {}",
                id, request.getNewStatus());

        RecycleRequestResponse response = recycleRequestService.updateFulfillmentStatus(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get fulfillment status history for a recycle request
     * 
     * Returns all status transitions in reverse chronological order
     * Useful for tracking, auditing, and debugging fulfillment workflow
     */
    @GetMapping("/{id}/status-history")
    public ResponseEntity<List<RecycleStatusHistory>> getStatusHistory(@PathVariable UUID id) {
        log.info("GET /api/v1/recycle-requests/{}/status-history", id);

        List<RecycleStatusHistory> history = recycleRequestService.getStatusHistory(id);
        return ResponseEntity.ok(history);
    }

    /**
     * Facility owner approves the request
     */
    @PutMapping("/{id}/approve")
    public ResponseEntity<RecycleRequestResponse> approveRequest(@PathVariable UUID id) {
        log.info("PUT /api/v1/recycle-requests/{}/approve", id);
        return ResponseEntity.ok(recycleRequestService.approveRequest(id));
    }

    /**
     * Assign a driver to the pickup request.
     * Triggers email with links.
     */
    @PostMapping("/{id}/assign-driver")
    public ResponseEntity<RecycleRequestResponse> assignDriver(
            @PathVariable UUID id,
            @Valid @RequestBody AssignDriverDto request) {
        log.info("POST /api/v1/recycle-requests/{}/assign-driver - driverId: {}", id, request.getDriverId());
        return ResponseEntity.ok(recycleRequestService.assignDriver(id, request));
    }

    /**
     * Driver webhook: Link 1 clicked from email. Direct pickup done.
     */
    @GetMapping("/driver-action/{id}/pickup-done")
    public ResponseEntity<RecycleRequestResponse> handleDriverPickupDone(
            @PathVariable UUID id,
            @RequestParam String token) {
        log.info("GET /api/v1/recycle-requests/driver-action/{}/pickup-done?token={}", id, token);
        return ResponseEntity.ok(recycleRequestService.handleDriverPickupDone(id, token));
    }

    /**
     * Driver webhook: Link 2 post form submission. Pickup failed with reason.
     */
    @PostMapping("/driver-action/{id}/pickup-failed")
    public ResponseEntity<RecycleRequestResponse> handleDriverPickupFailed(
            @PathVariable UUID id,
            @Valid @RequestBody DriverActionReasonDto request) {
        log.info("POST /api/v1/recycle-requests/driver-action/{}/pickup-failed - reason: {}", id, request.getReason());
        return ResponseEntity.ok(recycleRequestService.handleDriverPickupFailed(id, request));
    }

    /**
     * Officer verifies failure reason of locked request
     */
    @PutMapping("/{id}/verify-failure")
    public ResponseEntity<RecycleRequestResponse> verifyFailureAndLock(
            @PathVariable UUID id,
            @RequestParam RecycleStatus newStatus) {
        log.info("PUT /api/v1/recycle-requests/{}/verify-failure - newStatus: {}", id, newStatus);
        return ResponseEntity.ok(recycleRequestService.verifyFailureAndLock(id, newStatus));
    }

    /**
     * Cancel recycle request (by user)
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<RecycleRequestResponse> cancelRecycleRequest(
            @PathVariable UUID id,
            @RequestParam UUID userId) {
        log.info("PUT /api/v1/recycle-requests/{}/cancel - userId: {}", id, userId);
        RecycleRequestResponse response = recycleRequestService.cancelRequest(id, userId);
        return ResponseEntity.ok(response);
    }
}
