package com.elocate.elocate.service;

import com.elocate.elocate.dto.CreateRecycleRequestDto;
import com.elocate.elocate.dto.RecycleRequestResponse;
import com.elocate.elocate.dto.UpdateFulfillmentStatusDto;
import com.elocate.elocate.dto.VerifyRecycleRequestDto;
import com.elocate.elocate.dto.AssignDriverDto;
import com.elocate.elocate.dto.DriverActionReasonDto;
import com.elocate.elocate.exception.ConditionFactorNotFoundException;
import com.elocate.elocate.exception.ModelNotFoundException;
import com.elocate.elocate.exception.RecycleRequestNotFoundException;
import com.elocate.elocate.model.*;
import com.elocate.elocate.model.enums.FulfillmentStatus;
import com.elocate.elocate.model.enums.FulfillmentType;
import com.elocate.elocate.model.enums.MetalType;
import com.elocate.elocate.repository.DeviceConditionFactorRepository;
import com.elocate.elocate.repository.DeviceModelRepository;
import com.elocate.elocate.repository.RecycleRequestRepository;
import com.elocate.elocate.repository.RecyclingFacilityRepository;
import com.elocate.elocate.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import com.elocate.elocate.dto.FacilityWithDistanceProjection;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service for managing recycle requests with point calculation
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RecycleRequestService {

        private final RecycleRequestRepository recycleRequestRepository;
        private final DeviceModelRepository deviceModelRepository;
        private final DeviceConditionFactorRepository conditionFactorRepository;
        private final RewardCalculationService rewardCalculationService;
        private final RewardSnapshotService rewardSnapshotService;
        private final WalletService walletService;
        private final RecyclingFacilityRepository recyclingFacilityRepository;
        private final FulfillmentStatusValidator fulfillmentStatusValidator;
        private final RecycleStatusHistoryService statusHistoryService;
        private final EmailService emailService;
        private final UserRepository userRepository;

        /**
         * Create recycle request with estimated points and fulfillment tracking
         * 
         * Steps:
         * 1. Validate fulfillment requirements
         * 2. Fetch device model
         * 3. Fetch current metal rates
         * 4. Calculate base metal points
         * 5. Fetch condition multiplier
         * 6. Apply multiplier to get estimated points
         * 7. Handle fulfillment setup (pickup address or facility)
         * 8. Save recycle request
         * 9. Create reward snapshot (freeze calculation)
         * 10. Record initial status in history
         */
        @Transactional
        public RecycleRequestResponse createRecycleRequest(UUID userId, CreateRecycleRequestDto request) {
                log.info("Creating recycle request for user: {}, deviceModelId: {}, condition: {}, fulfillmentType: {}",
                                userId, request.getDeviceModelId(), request.getConditionCode(),
                                request.getFulfillmentType());

                // Step 1: Validate fulfillment requirements and get necessary IDs
                UserAddress pickupAddress = null;
                RecyclingFacility facility = null;

                if (request.getFulfillmentType() == FulfillmentType.PICKUP) {
                        pickupAddress = fulfillmentStatusValidator.validateAndGetPickupAddress(request, userId);
                        log.info("Pickup address resolved: {}", pickupAddress.getId());

                        // Handle facility assignment for PICKUP
                        if (request.getFacilityId() != null) {
                                facility = recyclingFacilityRepository.findById(request.getFacilityId())
                                                .orElseThrow(() -> new IllegalArgumentException(
                                                                "Facility not found: " + request.getFacilityId()));
                        } else if (pickupAddress.getLatitude() != null && pickupAddress.getLongitude() != null) {
                                // Find nearest facility within 100km
                                Page<FacilityWithDistanceProjection> nearest = recyclingFacilityRepository
                                                .findNearestFacilities(
                                                                pickupAddress.getLatitude().doubleValue(),
                                                                pickupAddress.getLongitude().doubleValue(),
                                                                100.0,
                                                                PageRequest.of(0, 1));
                                if (nearest.hasContent()) {
                                        UUID facilityId = nearest.getContent().get(0).getId();
                                        facility = recyclingFacilityRepository.findById(facilityId).orElse(null);
                                        log.info("Auto-assigned nearest facility by coordinates: {}",
                                                        facility != null ? facility.getName() : "None");
                                }
                        }

                        // Fallback if still no facility (either coordinates missing or none nearby)
                        if (facility == null) {
                                String searchTerm = pickupAddress.getCity() != null ? pickupAddress.getCity()
                                                : pickupAddress.getPincode();
                                if (searchTerm != null) {
                                        Page<RecyclingFacility> byLocality = recyclingFacilityRepository
                                                        .findByNameContainingIgnoreCaseOrAddressContainingIgnoreCase(
                                                                        searchTerm, searchTerm, PageRequest.of(0, 1));
                                        if (byLocality.hasContent()) {
                                                facility = byLocality.getContent().get(0);
                                                log.info("Auto-assigned facility by locality ({}): {}", searchTerm,
                                                                facility.getName());
                                        }
                                }
                        }
                } else if (request.getFulfillmentType() == FulfillmentType.DROP_OFF) {
                        fulfillmentStatusValidator.validateDropOffRequirements(request);
                        facility = recyclingFacilityRepository.findById(request.getFacilityId())
                                        .orElseThrow(() -> new IllegalArgumentException(
                                                        "Facility not found: " + request.getFacilityId()));
                        log.info("Drop-off facility assigned: {}", facility.getName());
                }

                // Step 2: Fetch device model
                DeviceModel deviceModel = deviceModelRepository.findById(request.getDeviceModelId())
                                .orElseThrow(() -> new ModelNotFoundException(request.getDeviceModelId()));

                // Step 3: Fetch current metal rates
                Map<MetalType, MetalRate> metalRates = rewardCalculationService.getCurrentMetalRates();

                // Step 4: Calculate base metal points
                BigDecimal basePoints = rewardCalculationService.calculateMetalPoints(deviceModel, metalRates);

                // Step 5: Fetch condition multiplier
                DeviceConditionFactor conditionFactor = conditionFactorRepository
                                .findByConditionCodeAndIsActive(request.getConditionCode(), true)
                                .orElseThrow(() -> new ConditionFactorNotFoundException(request.getConditionCode()));

                // Step 6: Apply multiplier
                BigDecimal estimatedAmount = rewardCalculationService.applyConditionMultiplier(
                                basePoints, conditionFactor.getMultiplier());

                // Step 7: Determine initial fulfillment status
                FulfillmentStatus initialStatus = fulfillmentStatusValidator
                                .getInitialStatus(request.getFulfillmentType());

                // Step 8: Save recycle request
                RecycleRequest.RecycleRequestBuilder builder = RecycleRequest.builder()
                                .userId(userId)
                                .deviceModel(deviceModel)
                                .conditionCode(request.getConditionCode())
                                .estimatedAmount(estimatedAmount)
                                .status(RecycleStatus.CREATED)
                                .fulfillmentType(request.getFulfillmentType())
                                .fulfillmentStatus(initialStatus);

                // Set pickup address if applicable
                if (pickupAddress != null) {
                        builder.pickupAddress(pickupAddress);
                }

                // Set facility if applicable
                if (facility != null) {
                        builder.recyclingFacility(facility);
                }

                RecycleRequest recycleRequest = builder.build();
                RecycleRequest saved = recycleRequestRepository.save(recycleRequest);
                log.info("Recycle request created with id: {}, estimated points: {}, fulfillment: {}/{}",
                                saved.getId(), estimatedAmount, saved.getFulfillmentType(),
                                saved.getFulfillmentStatus());

                // Step 9: Create reward snapshot (freeze rates at this moment)
                rewardSnapshotService.createSnapshot(
                                saved,
                                deviceModel,
                                metalRates,
                                conditionFactor.getMultiplier(),
                                estimatedAmount);

                // Step 10: Record initial status in history (both RecycleStatus and FulfillmentStatus)
                statusHistoryService.recordRecycleStatusChange(saved, null, RecycleStatus.CREATED, userId);
                statusHistoryService.recordStatusChange(saved, null, initialStatus, userId);

                // Step 11: Send email notifications
                try {
                        // Notify Citizen
                        User citizen = userRepository.findById(userId).orElse(null);
                        if (citizen != null && citizen.getEmail() != null) {
                                emailService.sendRequestCreatedEmail(
                                                citizen.getEmail(),
                                                saved.getId().toString(),
                                                deviceModel.getModelName(),
                                                estimatedAmount);
                        }

                        // Notify Facility
                        if (facility != null && facility.getEmail() != null) {
                                emailService.sendRequestAssignedToFacilityEmail(
                                                facility.getEmail(),
                                                saved.getId().toString(),
                                                deviceModel.getModelName());
                        }
                } catch (Exception e) {
                        log.error("Failed to send notification emails for request {}: {}", saved.getId(),
                                        e.getMessage());
                }

                return mapToResponse(saved);
        }

        /**
         * Get recycle requests for a user with optional status and search filters.
         *
         * Uses separate repository queries per filter combination to avoid passing
         * null bind parameters into JPQL, which causes PostgreSQL to fail with
         * "function lower(bytea) does not exist" when it cannot infer the parameter
         * type.
         */
        @Transactional(readOnly = true)
        public List<RecycleRequestResponse> getRecycleRequests(UUID userId, RecycleStatus status, String searchTerm) {
                log.info("Fetching recycle requests for user: {}, status: {}, search: {}", userId, status, searchTerm);

                boolean hasStatus = status != null;
                boolean hasSearch = searchTerm != null && !searchTerm.isBlank();

                List<RecycleRequest> results;
                if (hasStatus && hasSearch) {
                        results = recycleRequestRepository.findByUserIdAndStatusAndSearchTerm(userId, status,
                                        searchTerm);
                } else if (hasStatus) {
                        results = recycleRequestRepository.findByUserIdAndStatus(userId, status);
                } else if (hasSearch) {
                        results = recycleRequestRepository.findByUserIdAndSearchTerm(userId, searchTerm);
                } else {
                        results = recycleRequestRepository.findByUserId(userId);
                }

                return results.stream()
                                .map(this::mapToResponse)
                                .toList();
        }

        @Transactional(readOnly = true)
        public RecycleRequestResponse getRecycleRequestById(UUID id) {
                log.info("Fetching recycle request: {}", id);
                RecycleRequest request = recycleRequestRepository.findById(id)
                                .orElseThrow(() -> new com.elocate.elocate.exception.RecycleRequestNotFoundException(
                                                id));
                return mapToResponse(request);
        }

        @Transactional(readOnly = true)
        public List<RecycleRequestResponse> getRecycleRequestsByFacility(UUID facilityId, RecycleStatus status,
                        String searchTerm) {
                log.info("Fetching recycle requests for facility: {}", facilityId);
                // Simple implementation that doesn't yet filter by status/searchTerm
                List<RecycleRequest> results = recycleRequestRepository.findByFacilityId(facilityId);

                if (status != null) {
                        results = results.stream().filter(r -> r.getStatus() == status).toList();
                }
                if (searchTerm != null && !searchTerm.isBlank()) {
                        String lowerSearchTerm = searchTerm.toLowerCase();
                        results = results.stream().filter(r -> (r.getDeviceModel() != null
                                        && r.getDeviceModel().getModelName() != null
                                        && r.getDeviceModel().getModelName().toLowerCase().contains(lowerSearchTerm)) ||
                                        (r.getDeviceModel() != null && r.getDeviceModel().getBrand() != null
                                                        && r.getDeviceModel().getBrand().getName() != null
                                                        && r.getDeviceModel().getBrand().getName().toLowerCase()
                                                                        .contains(lowerSearchTerm)))
                                        .toList();
                }

                return results.stream()
                                .map(this::mapToResponse)
                                .toList();
        }

        /**
         * Verify recycle request and recalculate final points
         *
         * Steps:
         * 1. Fetch recycle request
         * 2. Fetch reward snapshot
         * 3. Recalculate points using verified weight/condition
         * 4. Update recycle request with final points
         * 5. Update status to VERIFIED
         * 6. Credit user wallet
         */
        @Transactional
        public RecycleRequestResponse verifyRecycleRequest(UUID id, VerifyRecycleRequestDto request) {
                log.info("Verifying recycle request: {}, verified weight: {}, condition: {}",
                                id, request.getVerifiedWeightGrams(), request.getVerifiedConditionCode());

                // Step 1: Fetch recycle request
                RecycleRequest recycleRequest = recycleRequestRepository.findById(id)
                                .orElseThrow(() -> new RecycleRequestNotFoundException(id));

                // Step 2: Fetch reward snapshot
                RewardSnapshot snapshot = rewardSnapshotService.getSnapshotByRequestId(id)
                                .orElseThrow(() -> new RuntimeException(
                                                "Reward snapshot not found for request: " + id));

                // Step 3: Recalculate (for now, we'll use the snapshot's multiplier and
                // recalculate)
                // In a more complex scenario, you might recalculate based on verified condition
                DeviceConditionFactor verifiedCondition = conditionFactorRepository
                                .findByConditionCodeAndIsActive(request.getVerifiedConditionCode(), true)
                                .orElseThrow(() -> new ConditionFactorNotFoundException(
                                                request.getVerifiedConditionCode()));

                // Use snapshot metal composition (frozen at creation time)
                BigDecimal basePoints = calculateBasePointsFromSnapshot(snapshot);
                BigDecimal finalAmount = rewardCalculationService.applyConditionMultiplier(
                                basePoints, verifiedCondition.getMultiplier());

                // Step 4 & 5: Update recycle request
                recycleRequest.setFinalAmount(finalAmount);
                recycleRequest.setStatus(RecycleStatus.VERIFIED);
                recycleRequest.setConditionCode(request.getVerifiedConditionCode());

                RecycleRequest updated = recycleRequestRepository.save(recycleRequest);
                log.info("Recycle request verified with final points: {}", finalAmount);

                // Step 6: Credit wallet
                walletService.creditWallet(
                                recycleRequest.getUserId(),
                                updated,
                                finalAmount,
                                "Recycle reward credited");

                return mapToResponse(updated);
        }

        /**
         * Calculate base points from snapshot (frozen metal composition)
         */
        private BigDecimal calculateBasePointsFromSnapshot(RewardSnapshot snapshot) {
                // Get current metal rates
                Map<MetalType, MetalRate> metalRates = rewardCalculationService.getCurrentMetalRates();

                BigDecimal totalPoints = BigDecimal.ZERO;

                // Calculate using frozen metal quantities from snapshot
                if (snapshot.getGoldMg() != null && metalRates.containsKey(MetalType.GOLD)) {
                        totalPoints = totalPoints.add(
                                        snapshot.getGoldMg()
                                                        .multiply(metalRates.get(MetalType.GOLD).getPointsPerUnit()));
                }

                if (snapshot.getSilverMg() != null && metalRates.containsKey(MetalType.SILVER)) {
                        totalPoints = totalPoints.add(
                                        snapshot.getSilverMg()
                                                        .multiply(metalRates.get(MetalType.SILVER).getPointsPerUnit()));
                }

                if (snapshot.getCopperG() != null && metalRates.containsKey(MetalType.COPPER)) {
                        totalPoints = totalPoints.add(
                                        snapshot.getCopperG()
                                                        .multiply(metalRates.get(MetalType.COPPER).getPointsPerUnit()));
                }

                if (snapshot.getPalladiumMg() != null && metalRates.containsKey(MetalType.PALLADIUM)) {
                        totalPoints = totalPoints.add(
                                        snapshot.getPalladiumMg().multiply(
                                                        metalRates.get(MetalType.PALLADIUM).getPointsPerUnit()));
                }

                return totalPoints;
        }

        /**
         * Get status history for a recycle request
         * 
         * @param recycleRequestId Recycle request ID
         * @return List of status history entries
         */
        @Transactional(readOnly = true)
        public List<RecycleStatusHistory> getStatusHistory(UUID recycleRequestId) {
                return statusHistoryService.getStatusHistory(recycleRequestId);
        }

        /**
         * Update fulfillment status of a recycle request
         * 
         * @param id  Recycle request ID
         * @param dto Status update DTO
         * @return Updated recycle request response
         */
        @Transactional
        public RecycleRequestResponse updateFulfillmentStatus(UUID id, UpdateFulfillmentStatusDto dto) {
                log.info("Updating fulfillment status for request: {}, new status: {}", id, dto.getNewStatus());

                RecycleRequest request = recycleRequestRepository.findById(id)
                                .orElseThrow(() -> new RecycleRequestNotFoundException(id));

                FulfillmentStatus oldStatus = request.getFulfillmentStatus();

                // Validate status transition
                fulfillmentStatusValidator.validateStatusTransition(oldStatus, dto.getNewStatus());

                // Update status
                request.setFulfillmentStatus(dto.getNewStatus());
                RecycleRequest updated = recycleRequestRepository.save(request);

                // Record status change in history
                statusHistoryService.recordStatusChange(updated, oldStatus, dto.getNewStatus(), dto.getChangedBy());

                log.info("Fulfillment status updated: {} -> {}", oldStatus, dto.getNewStatus());
                return mapToResponse(updated);
        }

        /**
         * Facility owner approves the request
         */
        @Transactional
        public RecycleRequestResponse approveRequest(UUID id) {
                RecycleRequest request = recycleRequestRepository.findById(id)
                                .orElseThrow(() -> new RecycleRequestNotFoundException(id));
                request.setStatus(RecycleStatus.APPROVED);
                log.info("Request {} approved by facility owner.", id);
                return mapToResponse(recycleRequestRepository.save(request));
        }

        /**
         * Assign driver and trigger email links
         */
        @Transactional
        public RecycleRequestResponse assignDriver(UUID id, AssignDriverDto dto) {
                RecycleRequest request = recycleRequestRepository.findById(id)
                                .orElseThrow(() -> new RecycleRequestNotFoundException(id));

                request.setAssignedDriverId(dto.getDriverId());
                FulfillmentStatus oldStatus = request.getFulfillmentStatus();
                request.setFulfillmentStatus(FulfillmentStatus.PICKUP_ASSIGNED);

                RecycleRequest saved = recycleRequestRepository.save(request);
                statusHistoryService.recordStatusChange(saved, oldStatus, FulfillmentStatus.PICKUP_ASSIGNED, null);

                // MOCK EMAIL SENDING
                String token = UUID.randomUUID().toString(); // In real app, generate secure expiring JWT or DB token
                log.info("📧 MOCK EMAIL TO DRIVER: You have been assigned to pickup request {}.", id);
                log.info("🔗 Link 1 (Pickup Done): GET /api/v1/recycle-requests/driver-action/{}/pickup-done?token={}",
                                id, token);
                log.info("🔗 Link 2 (Pickup Not Done): POST /api/v1/recycle-requests/driver-action/{}/pickup-failed",
                                id);

                return mapToResponse(saved);
        }

        /**
         * Driver action: direct pickup done from email link
         */
        @Transactional
        public RecycleRequestResponse handleDriverPickupDone(UUID id, String token) {
                log.info("Driver indicated pickup done directly from link for request {}, token: {}", id, token);
                // In a real app, validate the token here.

                RecycleRequest request = recycleRequestRepository.findById(id)
                                .orElseThrow(() -> new RecycleRequestNotFoundException(id));

                FulfillmentStatus oldStatus = request.getFulfillmentStatus();
                request.setFulfillmentStatus(FulfillmentStatus.PICKUP_COMPLETED);

                RecycleRequest saved = recycleRequestRepository.save(request);
                statusHistoryService.recordStatusChange(saved, oldStatus, FulfillmentStatus.PICKUP_COMPLETED,
                                request.getAssignedDriverId());

                return mapToResponse(saved);
        }

        /**
         * Driver action: pickup failed with reason
         */
        @Transactional
        public RecycleRequestResponse handleDriverPickupFailed(UUID id, DriverActionReasonDto dto) {
                log.info("Driver reported pickup failure for request {}, reason: {}", id, dto.getReason());

                RecycleRequest request = recycleRequestRepository.findById(id)
                                .orElseThrow(() -> new RecycleRequestNotFoundException(id));

                FulfillmentStatus oldStatus = request.getFulfillmentStatus();
                request.setFulfillmentStatus(FulfillmentStatus.PICKUP_FAILED);
                request.setDriverFailureReason(dto.getReason());

                // Set lock status
                request.setStatus(RecycleStatus.LOCKED);

                RecycleRequest saved = recycleRequestRepository.save(request);
                statusHistoryService.recordStatusChange(saved, oldStatus, FulfillmentStatus.PICKUP_FAILED,
                                request.getAssignedDriverId());

                return mapToResponse(saved);
        }

        /**
         * Officer verifies failure and resolves lock
         */
        @Transactional
        public RecycleRequestResponse verifyFailureAndLock(UUID id, RecycleStatus newStatus) {
                log.info("Officer resolving locked request {} to new status {}", id, newStatus);

                RecycleRequest request = recycleRequestRepository.findById(id)
                                .orElseThrow(() -> new RecycleRequestNotFoundException(id));

                if (request.getStatus() != RecycleStatus.LOCKED) {
                        throw new IllegalStateException("Request is not LOCKED");
                }

                request.setStatus(newStatus);
                return mapToResponse(recycleRequestRepository.save(request));
        }

        /**
         * Cancel a recycle request
         */
        @Transactional
        public RecycleRequestResponse cancelRequest(UUID id, UUID userId) {
                log.info("User {} cancelling request {}", userId, id);

                RecycleRequest request = recycleRequestRepository.findById(id)
                                .orElseThrow(() -> new RecycleRequestNotFoundException(id));

                // Security check
                if (!request.getUserId().equals(userId)) {
                        throw new IllegalStateException("Unauthorized to cancel this request");
                }

                // Business logic: only CREATED or APPROVED can be cancelled by user
                if (request.getStatus() != RecycleStatus.CREATED && request.getStatus() != RecycleStatus.APPROVED) {
                        throw new IllegalStateException(
                                        "Request cannot be cancelled in its current state: " + request.getStatus());
                }

                request.setStatus(RecycleStatus.CANCELLED);

                // Also update fulfillment status if it's not already completed
                FulfillmentStatus oldFulfillmentStatus = request.getFulfillmentStatus();
                // We'll set it to a terminal state if possible.
                // Note: If FulfillmentStatus doesn't have CANCELLED, we might need to add it or
                // just leave as is.
                // For now, let's keep it simple.

                RecycleRequest saved = recycleRequestRepository.save(request);

                // Record in history
                statusHistoryService.recordStatusChange(saved, oldFulfillmentStatus, oldFulfillmentStatus, userId);

                return mapToResponse(saved);
        }

        /**
         * Map entity to response DTO
         */
        private RecycleRequestResponse mapToResponse(RecycleRequest request) {
                RecycleRequestResponse.RecycleRequestResponseBuilder builder = RecycleRequestResponse.builder()
                                .id(request.getId())
                                .deviceModelId(request.getDeviceModel().getId())
                                .deviceModelName(request.getDeviceModel().getModelName())
                                .brandName(request.getDeviceModel().getBrand().getName())
                                .categoryName(request.getDeviceModel().getCategory().getName())
                                .conditionCode(request.getConditionCode())
                                .estimatedAmount(request.getEstimatedAmount())
                                .finalAmount(request.getFinalAmount())
                                .status(String.valueOf(request.getStatus()))
                                .fulfillmentType(request.getFulfillmentType())
                                .fulfillmentStatus(request.getFulfillmentStatus())
                                .fulfillmentStatusDisplay(request.getFulfillmentStatus().getDisplayText())
                                .assignedDriverId(request.getAssignedDriverId())
                                .driverFailureReason(request.getDriverFailureReason())
                                .pickupDate(request.getPickupDate())
                                .createdAt(request.getCreatedAt())
                                .updatedAt(request.getUpdatedAt());

                // Add pickup address details if present
                if (request.getPickupAddress() != null) {
                        UserAddress addr = request.getPickupAddress();
                        builder.pickupAddressId(addr.getId())
                                        .pickupAddress(addr.getAddress())
                                        .pickupCity(addr.getCity())
                                        .pickupState(addr.getState())
                                        .pickupPincode(addr.getPincode());
                }

                // Add facility info if present
                if (request.getRecyclingFacility() != null) {
                        builder.facilityId(request.getRecyclingFacility().getId())
                                        .facilityName(request.getRecyclingFacility().getName());
                }

                return builder.build();
        }
}
