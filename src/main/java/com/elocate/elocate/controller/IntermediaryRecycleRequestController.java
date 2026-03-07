package com.elocate.elocate.controller;

import com.elocate.elocate.dto.*;
import com.elocate.elocate.model.RecycleRequest;
import com.elocate.elocate.model.RecycleStatus;
import com.elocate.elocate.model.RecyclingFacility;
import com.elocate.elocate.repository.RecyclingFacilityRepository;
import com.elocate.elocate.service.EnhancedPricingService;
import com.elocate.elocate.service.IntermediaryService;
import com.elocate.elocate.service.RecycleRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.elocate.elocate.context.UserContextHolder;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Intermediary controller for recycle request management
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/intermediary/recycle-requests")
@RequiredArgsConstructor
@PreAuthorize("hasRole('INTERMEDIARY')")
public class IntermediaryRecycleRequestController {

        private final IntermediaryService intermediaryService;
        private final RecycleRequestService recycleRequestService;
        private final EnhancedPricingService pricingService;
        private final RecyclingFacilityRepository facilityRepository;

        /**
         * Get all recycle requests for intermediary's facility
         */
        @GetMapping
        public ResponseEntity<List<RecycleRequestResponse>> getRecycleRequests(
                        @RequestParam(required = false) RecycleStatus status,
                        @RequestParam(required = false) String search,
                        @RequestParam UUID userId) {

                log.info("Intermediary fetching requests for user ID: {}", userId);

                Optional<RecyclingFacility> facilityOpt = facilityRepository.findByUserId(userId);
                if (facilityOpt.isEmpty()) {
                        return ResponseEntity.ok(Collections.emptyList());
                }

                UUID facilityId = facilityOpt.get().getId();

                List<RecycleRequestResponse> requests = recycleRequestService
                                .getRecycleRequestsByFacility(facilityId, status, search);

                return ResponseEntity.ok(requests);
        }

        /**
         * Get recycle request by ID
         */
        @GetMapping("/{id}")
        public ResponseEntity<RecycleRequestResponse> getRecycleRequestById(@PathVariable UUID id) {
                log.info("Intermediary fetching request: {}", id);
                RecycleRequestResponse request = recycleRequestService.getRecycleRequestById(id);
                return ResponseEntity.ok(request);
        }

        /**
         * Get AI pricing for a request
         */
        @GetMapping("/{id}/ai-pricing")
        public ResponseEntity<PricingBreakdownResponse> getAIPricing(@PathVariable UUID id) {
                log.info("Intermediary fetching AI pricing for request: {}", id);

                RecycleRequestResponse request = recycleRequestService.getRecycleRequestById(id);
                // Get device model and calculate pricing
                // This would need the full device model entity

                return ResponseEntity.ok(PricingBreakdownResponse.builder()
                                .pricingSource("AI")
                                .build());
        }

        /**
         * Approve recycle request
         */
        @PostMapping("/{id}/approve")
        public ResponseEntity<Map<String, String>> approveRequest(
                        @PathVariable UUID id,
                        @Valid @RequestBody IntermediaryApprovalRequest request) {

                log.info("Intermediary approving request: {}", id);

                UUID facilityOwnerId = UserContextHolder.getContext().getUserId();
                intermediaryService.approveRequest(id, request, facilityOwnerId);

                return ResponseEntity.ok(Map.of(
                                "message", "Request approved successfully",
                                "requestId", id.toString()));
        }

        /**
         * Reject recycle request
         */
        @PostMapping("/{id}/reject")
        public ResponseEntity<Map<String, String>> rejectRequest(
                        @PathVariable UUID id,
                        @RequestBody Map<String, String> body) {

                log.info("Intermediary rejecting request: {}", id);

                UUID facilityOwnerId = UserContextHolder.getContext().getUserId();
                String reason = body.get("reason");

                if (reason == null || reason.isBlank()) {
                        return ResponseEntity.badRequest().body(Map.of("error", "Reason is required"));
                }

                intermediaryService.rejectRequest(id, reason, facilityOwnerId);

                return ResponseEntity.ok(Map.of(
                                "message", "Request rejected successfully",
                                "requestId", id.toString()));
        }

        /**
         * Update pricing after product receipt
         */
        @PostMapping("/{id}/update-pricing")
        public ResponseEntity<Map<String, String>> updatePricing(
                        @PathVariable UUID id,
                        @Valid @RequestBody IntermediaryPriceChangeRequest request) {

                log.info("Intermediary updating pricing for request: {}", id);

                UUID facilityOwnerId = UserContextHolder.getContext().getUserId();
                intermediaryService.updatePricingAfterReceipt(id, request, facilityOwnerId);

                return ResponseEntity.ok(Map.of(
                                "message", "Pricing updated successfully",
                                "requestId", id.toString()));
        }

        /**
         * Verify device condition
         */
        @PostMapping("/{id}/verify-condition")
        public ResponseEntity<Map<String, String>> verifyCondition(
                        @PathVariable UUID id,
                        @Valid @RequestBody ConditionVerificationRequest request) {

                log.info("Intermediary verifying condition for request: {}", id);

                UUID facilityOwnerId = UserContextHolder.getContext().getUserId();
                intermediaryService.verifyCondition(id, request, facilityOwnerId);

                return ResponseEntity.ok(Map.of(
                                "message", "Condition verified successfully",
                                "requestId", id.toString()));
        }

        /**
         * Mark request as recycled
         */
        @PostMapping("/{id}/mark-recycled")
        public ResponseEntity<Map<String, String>> markAsRecycled(
                        @PathVariable UUID id) {

                log.info("Intermediary marking request as recycled: {}", id);

                UUID facilityOwnerId = UserContextHolder.getContext().getUserId();
                intermediaryService.markAsRecycled(id, facilityOwnerId);

                return ResponseEntity.ok(Map.of(
                                "message", "Request marked as recycled successfully",
                                "requestId", id.toString()));
        }

        /**
         * Mark drop-off as received
         */
        @PostMapping("/{id}/mark-dropped")
        public ResponseEntity<Map<String, String>> markAsDropped(
                        @PathVariable UUID id) {

                log.info("Intermediary marking request as dropped: {}", id);

                UUID facilityOwnerId = UserContextHolder.getContext().getUserId();
                intermediaryService.markAsDropped(id, facilityOwnerId);

                return ResponseEntity.ok(Map.of(
                                "message", "Request marked as dropped successfully",
                                "requestId", id.toString()));
        }

        /**
         * Verify drop-off
         */
        @PostMapping("/{id}/verify-drop")
        public ResponseEntity<Map<String, String>> verifyDropOff(
                        @PathVariable UUID id) {

                log.info("Intermediary verifying drop-off for request: {}", id);

                UUID facilityOwnerId = UserContextHolder.getContext().getUserId();
                intermediaryService.verifyDropOff(id, facilityOwnerId);

                return ResponseEntity.ok(Map.of(
                                "message", "Drop-off verified successfully",
                                "requestId", id.toString()));
        }
}
