package com.elocate.elocate.service;

import com.elocate.elocate.dto.CreateRecycleRequestDto;
import com.elocate.elocate.dto.RecycleRequestResponse;
import com.elocate.elocate.dto.UpdateFulfillmentStatusDto;
import com.elocate.elocate.dto.VerifyRecycleRequestDto;
import com.elocate.elocate.dto.AssignDriverDto;
import com.elocate.elocate.dto.DriverActionReasonDto;
import com.elocate.elocate.dto.SendReminderDto;
import com.elocate.elocate.exception.ConditionFactorNotFoundException;
import com.elocate.elocate.exception.ModelNotFoundException;
import com.elocate.elocate.exception.RecycleRequestNotFoundException;
import com.elocate.elocate.model.*;
import com.elocate.elocate.model.enums.FulfillmentStatus;
import com.elocate.elocate.model.enums.FulfillmentType;
import com.elocate.elocate.model.enums.MetalType;
import com.elocate.elocate.repository.DeviceConditionFactorRepository;
import com.elocate.elocate.repository.DeviceModelRepository;
import com.elocate.elocate.repository.DriverRepository;
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
        private final DriverRepository driverRepository;
        private final DriverPickupService driverPickupService;
        private final RequestNumberGenerator requestNumberGenerator;

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

                // Step 8: Generate unique request number
                String requestNumber = requestNumberGenerator.generateUniqueRequestNumber();
                log.info("Generated request number: {}", requestNumber);

                // Step 9: Save recycle request
                RecycleRequest.RecycleRequestBuilder builder = RecycleRequest.builder()
                                .requestNumber(requestNumber)
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
                log.info("Recycle request created with id: {}, requestNumber: {}, estimated points: {}, fulfillment: {}/{}",
                                saved.getId(), saved.getRequestNumber(), estimatedAmount, saved.getFulfillmentType(),
                                saved.getFulfillmentStatus());

                // Step 10: Create reward snapshot (freeze rates at this moment)
                rewardSnapshotService.createSnapshot(
                                saved,
                                deviceModel,
                                metalRates,
                                conditionFactor.getMultiplier(),
                                estimatedAmount);

                // Step 11: Record initial status in history (both RecycleStatus and
                // FulfillmentStatus)
                statusHistoryService.recordRecycleStatusChange(saved, null, RecycleStatus.CREATED, userId);
                statusHistoryService.recordStatusChange(saved, null, initialStatus, userId);

                // Step 12: Send email notifications
                try {
                        // Notify Citizen
                        User citizen = userRepository.findById(userId).orElse(null);
                        if (citizen != null && citizen.getEmail() != null) {
                                emailService.sendRequestCreatedEmail(
                                                citizen.getEmail(),
                                                saved.getRequestNumber(),
                                                deviceModel.getModelName(),
                                                estimatedAmount);
                        }

                        // Notify Facility
                        if (facility != null && facility.getEmail() != null) {
                                emailService.sendRequestAssignedToFacilityEmail(
                                                facility.getEmail(),
                                                saved.getRequestNumber(),
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

        /**
         * Get all recycle requests for admin view with optional filters.
         */
        @Transactional(readOnly = true)
        public List<RecycleRequestResponse> getAllRecycleRequests(RecycleStatus status, String searchTerm) {
                log.info("Fetching all recycle requests for admin view. status: {}, search: {}", status, searchTerm);

                boolean hasStatus = status != null;
                boolean hasSearch = searchTerm != null && !searchTerm.isBlank();

                List<RecycleRequest> results;
                if (hasStatus && hasSearch) {
                        results = recycleRequestRepository.findByStatusAndSearchTermWithDetails(status, searchTerm);
                } else if (hasStatus) {
                        results = recycleRequestRepository.findByStatusWithDetails(status);
                } else if (hasSearch) {
                        results = recycleRequestRepository.findBySearchTermWithDetails(searchTerm);
                } else {
                        results = recycleRequestRepository.findAllWithDetails();
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
                
                // Record status change in history
                statusHistoryService.recordRecycleStatusChange(updated, RecycleStatus.CREATED, RecycleStatus.VERIFIED, null, "Device condition verified successfully");
                
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
                
                RecycleStatus oldStatus = request.getStatus();
                request.setStatus(RecycleStatus.APPROVED);
                
                RecycleRequest saved = recycleRequestRepository.save(request);
                
                // Record status change in history
                statusHistoryService.recordRecycleStatusChange(saved, oldStatus, RecycleStatus.APPROVED, null, "Request approved by facility owner");

                log.info("Request {} approved by facility owner.", id);
                return mapToResponse(saved);
        }

        /**
         * Assign driver and trigger email links
         */
        @Transactional
        public RecycleRequestResponse assignDriver(UUID id, AssignDriverDto dto) {
                RecycleRequest request = recycleRequestRepository.findById(id)
                                .orElseThrow(() -> new RecycleRequestNotFoundException(id));

                // Get driver details
                Driver driver = driverRepository.findById(dto.getDriverId())
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Driver not found with id: " + dto.getDriverId()));

                request.setAssignedDriverId(dto.getDriverId());
                FulfillmentStatus oldStatus = request.getFulfillmentStatus();
                request.setFulfillmentStatus(FulfillmentStatus.PICKUP_ASSIGNED);

                RecycleRequest saved = recycleRequestRepository.save(request);

                // Record status change with comments
                String historyComment = dto.getComments() != null && !dto.getComments().isBlank()
                                ? "Driver assigned with instructions: " + dto.getComments()
                                : "Driver assigned for pickup";
                statusHistoryService.recordStatusChange(saved, oldStatus, FulfillmentStatus.PICKUP_ASSIGNED,
                                null, historyComment);

                // Generate tokens and send email with frontend links and assignment comments
                driverPickupService.generateTokensAndSendEmail(id, dto.getDriverId(), dto.getComments());

                log.info("✅ Driver {} assigned to pickup request {} - Email sent with action links",
                                driver.getName(), id);

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

                RecycleStatus oldStatus = request.getStatus();
                request.setStatus(newStatus);
                
                RecycleRequest saved = recycleRequestRepository.save(request);
                
                // Record status change in history
                statusHistoryService.recordRecycleStatusChange(saved, oldStatus, newStatus, null, "Officer verified failure and resolved lock");

                return mapToResponse(saved);
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

                RecycleRequest saved = recycleRequestRepository.save(request);

                // Record in history for the Recycle Status specifically canceling
                statusHistoryService.recordRecycleStatusChange(saved, request.getStatus() == RecycleStatus.CANCELLED ? RecycleStatus.CREATED : request.getStatus(), RecycleStatus.CANCELLED, userId, "Request manually cancelled by user");

                return mapToResponse(saved);
        }

        /**
         * Reassign driver for a pickup request
         * Invalidates old driver's tokens and sends new email to new driver
         */
        @Transactional
        public RecycleRequestResponse reassignDriver(UUID id, AssignDriverDto dto) {
                RecycleRequest request = recycleRequestRepository.findById(id)
                                .orElseThrow(() -> new RecycleRequestNotFoundException(id));

                // Validate current status
                if (request.getFulfillmentStatus() != FulfillmentStatus.PICKUP_ASSIGNED) {
                        throw new IllegalStateException(
                                        "Cannot reassign driver. Current status: " + request.getFulfillmentStatus());
                }

                UUID oldDriverId = request.getAssignedDriverId();

                // Get new driver details
                Driver newDriver = driverRepository.findById(dto.getDriverId())
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Driver not found with id: " + dto.getDriverId()));

                // Invalidate old driver's tokens
                int invalidatedCount = driverPickupService.invalidateTokensForRequest(id);
                log.info("Invalidated {} tokens for request {} during driver reassignment", invalidatedCount, id);

                // Update driver assignment
                request.setAssignedDriverId(dto.getDriverId());
                FulfillmentStatus oldStatus = request.getFulfillmentStatus();
                request.setFulfillmentStatus(FulfillmentStatus.PICKUP_ASSIGNED);

                RecycleRequest saved = recycleRequestRepository.save(request);

                // Record status change with reassignment reason
                String historyComment = String.format("Driver reassigned from %s to %s",
                                oldDriverId, dto.getDriverId());
                if (dto.getComments() != null && !dto.getComments().isBlank()) {
                        historyComment += ". Reason: " + dto.getComments();
                }
                statusHistoryService.recordStatusChange(saved, oldStatus, FulfillmentStatus.PICKUP_ASSIGNED,
                                null, historyComment);

                // Generate new tokens and send email to new driver
                driverPickupService.generateTokensAndSendEmail(id, dto.getDriverId(), dto.getComments());

                log.info("✅ Driver reassigned for request {}. Old: {}, New: {} - Email sent with action links",
                                id, oldDriverId, newDriver.getName());

                return mapToResponse(saved);
        }

        /**
         * Send reminder to intermediary/facility owner about pending request
         */
        @Transactional
        public RecycleRequestResponse sendReminderToIntermediary(UUID requestId, UUID userId, String comment) {
                log.info("Sending reminder for request {} from user {}", requestId, userId);

                RecycleRequest request = recycleRequestRepository.findById(requestId)
                                .orElseThrow(() -> new RecycleRequestNotFoundException(requestId));

                // Security check - only request owner can send reminder
                if (!request.getUserId().equals(userId)) {
                        throw new IllegalStateException("Unauthorized to send reminder for this request");
                }

                // Get facility details
                RecyclingFacility facility = request.getRecyclingFacility();
                if (facility == null) {
                        throw new IllegalStateException("No facility assigned to this request");
                }

                if (facility.getEmail() == null || facility.getEmail().isBlank()) {
                        throw new IllegalStateException("Facility does not have an email address configured");
                }

                // Get citizen details
                User citizen = userRepository.findById(userId).orElse(null);
                String citizenName = citizen != null ? citizen.getFullName() : "Citizen";
                String citizenEmail = citizen != null ? citizen.getEmail() : "N/A";

                // Record in status history
                String historyComment = "Reminder sent to facility";
                if (comment != null && !comment.isBlank()) {
                        historyComment += ": " + comment;
                }
                statusHistoryService.recordStatusChange(
                                request,
                                request.getFulfillmentStatus(),
                                request.getFulfillmentStatus(),
                                userId,
                                historyComment);

                // Send email to facility
                try {
                        String dashboardUrl = "http://localhost:3000/intermediary/collections/" + requestId;
                        String deviceName = request.getDeviceModel().getBrand().getName() + " "
                                        + request.getDeviceModel().getModelName();
                        String currentStatus = request.getFulfillmentStatus().getDisplayText();
                        String submittedDate = request.getCreatedAt().toString();

                        emailService.sendReminderToIntermediaryEmail(
                                        facility.getEmail(),
                                        request.getRequestNumber(),
                                        deviceName,
                                        citizenName,
                                        citizenEmail,
                                        currentStatus,
                                        submittedDate,
                                        comment,
                                        dashboardUrl);

                        log.info("✅ Reminder email sent to facility {} for request {}", facility.getName(), requestId);
                } catch (Exception e) {
                        log.error("Failed to send reminder email for request {}: {}", requestId, e.getMessage());
                        throw new RuntimeException("Failed to send reminder email: " + e.getMessage());
                }

                return mapToResponse(request);
        }

        /**
         * Map entity to response DTO
         */
        private RecycleRequestResponse mapToResponse(RecycleRequest request) {
                // Fetch citizen details
                User citizen = userRepository.findById(request.getUserId()).orElse(null);
                String citizenName = citizen != null ? citizen.getFullName() : "Unknown Citizen";
                String citizenEmail = citizen != null ? citizen.getEmail() : "N/A";

                RecycleRequestResponse.RecycleRequestResponseBuilder builder = RecycleRequestResponse.builder()
                                .id(request.getId())
                                .requestNumber(request.getRequestNumber())
                                .userId(request.getUserId())
                                .citizenName(citizenName)
                                .citizenEmail(citizenEmail)
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
                                .certificateUrl(request.getCertificateUrl())
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
                        RecyclingFacility fac = request.getRecyclingFacility();
                        builder.facilityId(fac.getId())
                                        .facilityName(fac.getName())
                                        .facilityAddress(fac.getAddress())
                                        .facilityEmail(fac.getEmail())
                                        .facilityPhone(fac.getContactNumber());
                }

                return builder.build();
        }
}
