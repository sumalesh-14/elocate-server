package com.elocate.elocate.service;

import com.elocate.elocate.dto.IntermediaryApprovalRequest;
import com.elocate.elocate.dto.IntermediaryPriceChangeRequest;
import com.elocate.elocate.dto.ConditionVerificationRequest;
import com.elocate.elocate.exception.RecycleRequestNotFoundException;
import com.elocate.elocate.model.*;
import com.elocate.elocate.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Service for intermediary (facility owner) operations
 * Handles request approval, pricing adjustments, condition verification, and
 * recycling completion
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class IntermediaryService {

    private final RecycleRequestRepository recycleRequestRepository;
    private final UserRepository userRepository;
    private final WalletService walletService;
    private final EmailService emailService;
    private final EnhancedPricingService pricingService;
    private final RecycleStatusHistoryService statusHistoryService;
    private final CertificateService certificateService;

    /**
     * Approve recycle request with pricing confirmation
     */
    @Transactional
    public void approveRequest(UUID requestId, IntermediaryApprovalRequest request, UUID facilityOwnerId) {
        log.info("Intermediary {} approving request {}", facilityOwnerId, requestId);

        RecycleRequest recycleRequest = recycleRequestRepository.findById(requestId)
                .orElseThrow(() -> new RecycleRequestNotFoundException(requestId));

        // Validate status
        if (recycleRequest.getStatus() != RecycleStatus.CREATED) {
            throw new IllegalStateException("Can only approve requests in CREATED status");
        }

        // Update estimated points if adjusted
        if (request.getAdjustedEstimatedAmount() != null) {
            BigDecimal oldPoints = recycleRequest.getEstimatedAmount();
            recycleRequest.setEstimatedAmount(request.getAdjustedEstimatedAmount());

            log.info("Pricing adjusted from {} to {} for request {}",
                    oldPoints, request.getAdjustedEstimatedAmount(), requestId);
        }

        // Save AI analysis payload
        if (request.getAiPricingResponse() != null) {
            recycleRequest.setAiPricingResponse(request.getAiPricingResponse());
        }

        // Update status to APPROVED
        RecycleStatus oldStatus = recycleRequest.getStatus();
        recycleRequest.setStatus(RecycleStatus.APPROVED);
        recycleRequestRepository.save(recycleRequest);

        // Record RecycleStatus change in history
        statusHistoryService.recordRecycleStatusChange(
                recycleRequest,
                oldStatus,
                RecycleStatus.APPROVED,
                facilityOwnerId);

        // Send email notification to citizen
        User citizen = userRepository.findById(recycleRequest.getUserId()).orElse(null);
        if (citizen != null && citizen.getEmail() != null) {
            emailService.sendRequestApprovedEmail(
                    citizen.getEmail(),
                    requestId.toString(),
                    recycleRequest.getEstimatedAmount());
        }

        log.info("Request {} approved successfully", requestId);
    }

    /**
     * Reject recycle request
     */
    @Transactional
    public void rejectRequest(UUID requestId, String reason, UUID facilityOwnerId) {
        log.info("Intermediary {} rejecting request {}", facilityOwnerId, requestId);

        RecycleRequest recycleRequest = recycleRequestRepository.findById(requestId)
                .orElseThrow(() -> new RecycleRequestNotFoundException(requestId));

        // Validate status
        if (recycleRequest.getStatus() != RecycleStatus.CREATED) {
            throw new IllegalStateException("Can only reject requests in CREATED status");
        }

        // Update status to REJECTED
        recycleRequest.setStatus(RecycleStatus.REJECTED);
        recycleRequest.setFulfillmentStatus(com.elocate.elocate.model.enums.FulfillmentStatus.REJECTED);
        recycleRequestRepository.save(recycleRequest);

        // Record status change
        // statusHistoryService.recordStatusChange(...) - only for fulfillment status,
        // skip for rejection

        log.info("Request {} rejected", requestId);
    }

    /**
     * Update pricing after product receipt (before recycling)
     */
    @Transactional
    public void updatePricingAfterReceipt(UUID requestId, IntermediaryPriceChangeRequest request,
            UUID facilityOwnerId) {
        log.info("Intermediary {} updating pricing for request {}", facilityOwnerId, requestId);

        RecycleRequest recycleRequest = recycleRequestRepository.findById(requestId)
                .orElseThrow(() -> new RecycleRequestNotFoundException(requestId));

        // Validate status - can only change price after product is received but before
        // recycling
        if (recycleRequest.getStatus() == RecycleStatus.RECYCLED) {
            throw new IllegalStateException("Cannot change price after recycling is complete");
        }

        if (recycleRequest.getFulfillmentStatus() != com.elocate.elocate.model.enums.FulfillmentStatus.PICKUP_COMPLETED
                &&
                recycleRequest
                        .getFulfillmentStatus() != com.elocate.elocate.model.enums.FulfillmentStatus.DROPPED_AT_FACILITY
                &&
                recycleRequest
                        .getFulfillmentStatus() != com.elocate.elocate.model.enums.FulfillmentStatus.DROP_VERIFIED) {
            throw new IllegalStateException("Can only change price after product is received");
        }

        BigDecimal oldPoints = recycleRequest.getFinalAmount();
        recycleRequest.setFinalAmount(request.getNewFinalAmount());
        recycleRequestRepository.save(recycleRequest);

        log.info("Final points updated from {} to {} for request {}",
                oldPoints, request.getNewFinalAmount(), requestId);

        // Send email notification to citizen
        User citizen = userRepository.findById(recycleRequest.getUserId()).orElse(null);
        if (citizen != null && citizen.getEmail() != null) {
            emailService.sendPriceChangeNotification(
                    citizen.getEmail(),
                    requestId.toString(),
                    request.getReason());
        }
    }

    /**
     * Verify device condition after receipt
     */
    @Transactional
    public void verifyCondition(UUID requestId, ConditionVerificationRequest request, UUID facilityOwnerId) {
        log.info("Intermediary {} verifying condition for request {}", facilityOwnerId, requestId);

        RecycleRequest recycleRequest = recycleRequestRepository.findById(requestId)
                .orElseThrow(() -> new RecycleRequestNotFoundException(requestId));

        // Validate status
        if (recycleRequest.getFulfillmentStatus() != com.elocate.elocate.model.enums.FulfillmentStatus.PICKUP_COMPLETED
                &&
                recycleRequest
                        .getFulfillmentStatus() != com.elocate.elocate.model.enums.FulfillmentStatus.DROP_VERIFIED) {
            throw new IllegalStateException("Can only verify condition after product is received");
        }

        // Update condition code if provided
        if (request.getVerifiedConditionCode() != null) {
            recycleRequest.setConditionCode(request.getVerifiedConditionCode());
        }
        
        // Update final amount if provided
        if (request.getFinalAmount() != null) {
            recycleRequest.setFinalAmount(request.getFinalAmount());
        }

        // Update status to VERIFIED
        RecycleStatus oldStatus = recycleRequest.getStatus();
        recycleRequest.setStatus(RecycleStatus.VERIFIED);
        recycleRequestRepository.save(recycleRequest);

        // Record RecycleStatus change in history with comments
        String comments = request.getNotes() != null && !request.getNotes().isBlank() 
            ? request.getNotes() 
            : "Device condition verified";
        statusHistoryService.recordRecycleStatusChange(
                recycleRequest,
                oldStatus,
                RecycleStatus.VERIFIED,
                facilityOwnerId,
                comments);

        log.info("Condition verified for request {}", requestId);
    }

    /**
     * Mark request as recycled and credit wallet
     */
    @Transactional
    public void markAsRecycled(UUID requestId, UUID facilityOwnerId) {
        log.info("Intermediary {} marking request {} as recycled", facilityOwnerId, requestId);

        RecycleRequest recycleRequest = recycleRequestRepository.findById(requestId)
                .orElseThrow(() -> new RecycleRequestNotFoundException(requestId));

        // Validate status
        if (recycleRequest.getStatus() != RecycleStatus.VERIFIED) {
            throw new IllegalStateException("Can only mark as recycled after condition verification");
        }

        // Determine final points (use final_amount if set, otherwise estimated_amount)
        BigDecimal finalAmount = recycleRequest.getFinalAmount() != null ? recycleRequest.getFinalAmount()
                : recycleRequest.getEstimatedAmount();

        if (finalAmount == null || finalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Final points must be set and positive");
        }

        // Update status to RECYCLED
        RecycleStatus oldStatus = recycleRequest.getStatus();
        recycleRequest.setStatus(RecycleStatus.RECYCLED);

        // Ensure final points is set
        if (recycleRequest.getFinalAmount() == null) {
            recycleRequest.setFinalAmount(finalAmount);
        }
        
        // Get citizen for certificate generation
        User citizen = userRepository.findById(recycleRequest.getUserId())
                .orElseThrow(() -> new IllegalStateException("Citizen not found"));
        
        // Generate certificate and upload to S3
        String certificateUrl = certificateService.generateAndUploadCertificate(recycleRequest, citizen);
        recycleRequest.setCertificateUrl(certificateUrl);

        recycleRequestRepository.save(recycleRequest);

        // Record RecycleStatus change in history
        statusHistoryService.recordRecycleStatusChange(
                recycleRequest,
                oldStatus,
                RecycleStatus.RECYCLED,
                facilityOwnerId);

        // Credit wallet
        walletService.creditWallet(
                recycleRequest.getUserId(),
                recycleRequest,
                finalAmount,
                "Recycling reward for request " + requestId);

        // Send email notification to citizen with certificate link
        if (citizen.getEmail() != null) {
            BigDecimal monetaryAmount = pricingService.convertPointsToMoney(finalAmount);
            emailService.sendRecyclingCompletedWithCertificateEmail(
                    citizen.getEmail(),
                    requestId.toString(),
                    monetaryAmount,
                    certificateUrl);
        }

        log.info("Request {} marked as recycled, wallet credited with {} points, certificate generated", 
                requestId, finalAmount);
    }

    /**
     * Mark drop-off as received at facility
     */
    @Transactional
    public void markAsDropped(UUID requestId, UUID facilityOwnerId) {
        log.info("Intermediary {} marking request {} as dropped at facility", facilityOwnerId, requestId);

        RecycleRequest recycleRequest = recycleRequestRepository.findById(requestId)
                .orElseThrow(() -> new RecycleRequestNotFoundException(requestId));

        // Validate fulfillment type
        if (recycleRequest.getFulfillmentType() != com.elocate.elocate.model.enums.FulfillmentType.DROP_OFF) {
            throw new IllegalStateException("Can only mark DROP_OFF requests as dropped");
        }

        // Update fulfillment status
        com.elocate.elocate.model.enums.FulfillmentStatus oldFulfillmentStatus = recycleRequest.getFulfillmentStatus();
        recycleRequest.setFulfillmentStatus(com.elocate.elocate.model.enums.FulfillmentStatus.DROPPED_AT_FACILITY);
        recycleRequestRepository.save(recycleRequest);

        // Record status change
        statusHistoryService.recordStatusChange(
                recycleRequest,
                oldFulfillmentStatus,
                recycleRequest.getFulfillmentStatus(),
                facilityOwnerId);

        // Send email notification to citizen
        User citizen = userRepository.findById(recycleRequest.getUserId()).orElse(null);
        if (citizen != null && citizen.getEmail() != null) {
            emailService.sendDeviceReceivedEmail(citizen.getEmail(), requestId.toString());
        }

        log.info("Request {} marked as dropped at facility", requestId);
    }

    /**
     * Verify drop-off
     */
    @Transactional
    public void verifyDropOff(UUID requestId, UUID facilityOwnerId) {
        log.info("Intermediary {} verifying drop-off for request {}", facilityOwnerId, requestId);

        RecycleRequest recycleRequest = recycleRequestRepository.findById(requestId)
                .orElseThrow(() -> new RecycleRequestNotFoundException(requestId));

        // Validate status
        if (recycleRequest
                .getFulfillmentStatus() != com.elocate.elocate.model.enums.FulfillmentStatus.DROPPED_AT_FACILITY) {
            throw new IllegalStateException("Can only verify after device is dropped at facility");
        }

        // Update fulfillment status
        com.elocate.elocate.model.enums.FulfillmentStatus oldFulfillmentStatus = recycleRequest.getFulfillmentStatus();
        recycleRequest.setFulfillmentStatus(com.elocate.elocate.model.enums.FulfillmentStatus.DROP_VERIFIED);
        recycleRequestRepository.save(recycleRequest);

        // Record status change
        statusHistoryService.recordStatusChange(
                recycleRequest,
                oldFulfillmentStatus,
                recycleRequest.getFulfillmentStatus(),
                facilityOwnerId);

        log.info("Drop-off verified for request {}", requestId);
    }
}
