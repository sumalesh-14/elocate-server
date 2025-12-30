package com.elocate.elocate.service;

import com.elocate.elocate.dto.CreateRecycleRequestDto;
import com.elocate.elocate.dto.RecycleRequestResponse;
import com.elocate.elocate.dto.UpdateFulfillmentStatusDto;
import com.elocate.elocate.dto.VerifyRecycleRequestDto;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                userId, request.getDeviceModelId(), request.getConditionCode(), request.getFulfillmentType());
        
        // Step 1: Validate fulfillment requirements and get necessary IDs
        UserAddress pickupAddress = null;
        RecyclingFacility facility = null;
        
        if (request.getFulfillmentType() == FulfillmentType.PICKUP) {
            pickupAddress = fulfillmentStatusValidator.validateAndGetPickupAddress(request, userId);
            log.info("Pickup address resolved: {}", pickupAddress.getId());
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
        BigDecimal estimatedPoints = rewardCalculationService.applyConditionMultiplier(
                basePoints, conditionFactor.getMultiplier());
        
        // Step 7: Determine initial fulfillment status
        FulfillmentStatus initialStatus = fulfillmentStatusValidator.getInitialStatus(request.getFulfillmentType());
        
        // Step 8: Save recycle request
        RecycleRequest.RecycleRequestBuilder builder = RecycleRequest.builder()
                .userId(userId)
                .deviceModel(deviceModel)
                .conditionCode(request.getConditionCode())
                .estimatedPoints(estimatedPoints)
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
                saved.getId(), estimatedPoints, saved.getFulfillmentType(), saved.getFulfillmentStatus());
        
        // Step 9: Create reward snapshot (freeze rates at this moment)
        rewardSnapshotService.createSnapshot(
                saved, 
                deviceModel, 
                metalRates, 
                conditionFactor.getMultiplier(), 
                estimatedPoints);
        
        // Step 10: Record initial status in history
        statusHistoryService.recordStatusChange(saved, null, initialStatus, userId);
        
        return mapToResponse(saved);
    }
    
    /**
     * Get recycle request by ID
     */
    @Transactional(readOnly = true)
    public RecycleRequestResponse getRecycleRequestById(UUID id) {
        log.info("Fetching recycle request: {}", id);
        RecycleRequest request = recycleRequestRepository.findById(id)
                .orElseThrow(() -> new RecycleRequestNotFoundException(id));
        return mapToResponse(request);
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
                .orElseThrow(() -> new RuntimeException("Reward snapshot not found for request: " + id));
        
        // Step 3: Recalculate (for now, we'll use the snapshot's multiplier and recalculate)
        // In a more complex scenario, you might recalculate based on verified condition
        DeviceConditionFactor verifiedCondition = conditionFactorRepository
                .findByConditionCodeAndIsActive(request.getVerifiedConditionCode(), true)
                .orElseThrow(() -> new ConditionFactorNotFoundException(request.getVerifiedConditionCode()));
        
        // Use snapshot metal composition (frozen at creation time)
        BigDecimal basePoints = calculateBasePointsFromSnapshot(snapshot);
        BigDecimal finalPoints = rewardCalculationService.applyConditionMultiplier(
                basePoints, verifiedCondition.getMultiplier());
        
        // Step 4 & 5: Update recycle request
        recycleRequest.setFinalPoints(finalPoints);
        recycleRequest.setStatus(RecycleStatus.VERIFIED);
        recycleRequest.setConditionCode(request.getVerifiedConditionCode());
        
        RecycleRequest updated = recycleRequestRepository.save(recycleRequest);
        log.info("Recycle request verified with final points: {}", finalPoints);
        
        // Step 6: Credit wallet
        walletService.creditWallet(
                recycleRequest.getUserId(),
                updated,
                finalPoints, 
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
                    snapshot.getGoldMg().multiply(metalRates.get(MetalType.GOLD).getPointsPerUnit()));
        }
        
        if (snapshot.getSilverMg() != null && metalRates.containsKey(MetalType.SILVER)) {
            totalPoints = totalPoints.add(
                    snapshot.getSilverMg().multiply(metalRates.get(MetalType.SILVER).getPointsPerUnit()));
        }
        
        if (snapshot.getCopperG() != null && metalRates.containsKey(MetalType.COPPER)) {
            totalPoints = totalPoints.add(
                    snapshot.getCopperG().multiply(metalRates.get(MetalType.COPPER).getPointsPerUnit()));
        }
        
        if (snapshot.getPalladiumMg() != null && metalRates.containsKey(MetalType.PALLADIUM)) {
            totalPoints = totalPoints.add(
                    snapshot.getPalladiumMg().multiply(metalRates.get(MetalType.PALLADIUM).getPointsPerUnit()));
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
     * @param id Recycle request ID
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
                .estimatedPoints(request.getEstimatedPoints())
                .finalPoints(request.getFinalPoints())
                .status(String.valueOf(request.getStatus()))
                .fulfillmentType(request.getFulfillmentType())
                .fulfillmentStatus(request.getFulfillmentStatus())
                .fulfillmentStatusDisplay(request.getFulfillmentStatus().getDisplayText())
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt());
        
        // Add pickup address if present
        if (request.getPickupAddress() != null) {
            builder.pickupAddressId(request.getPickupAddress().getId());
        }
        
        // Add facility info if present
        if (request.getRecyclingFacility() != null) {
            builder.facilityId(request.getRecyclingFacility().getId())
                   .facilityName(request.getRecyclingFacility().getName());
        }
        
        return builder.build();
    }
}

