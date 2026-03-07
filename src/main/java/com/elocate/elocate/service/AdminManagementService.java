package com.elocate.elocate.service;

import com.elocate.elocate.dto.AdminPriceChangeRequest;
import com.elocate.elocate.dto.AdminPriceOverrideRequest;
import com.elocate.elocate.dto.FacilityReassignmentRequest;
import com.elocate.elocate.exception.RecycleRequestNotFoundException;
import com.elocate.elocate.model.*;
import com.elocate.elocate.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service for admin management operations
 * Handles facility approval, pricing overrides, request reassignment, etc.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AdminManagementService {

        private final RecyclingFacilityRepository facilityRepository;
        private final RecycleRequestRepository recycleRequestRepository;
        private final DeviceModelRepository deviceModelRepository;
        private final UserRepository userRepository;
        private final AdminAuditLogService auditLogService;
        private final WalletService walletService;
        private final EmailService emailService;

        /**
         * Approve a recycling facility
         */
        @Transactional
        public void approveFacility(UUID facilityId, UUID adminUserId, String notes) {
                log.info("Admin {} approving facility {}", adminUserId, facilityId);

                RecyclingFacility facility = facilityRepository.findById(facilityId)
                                .orElseThrow(() -> new IllegalArgumentException("Facility not found: " + facilityId));

                String oldStatus = facility.getIsVerified() != null && facility.getIsVerified() ? "APPROVED"
                                : "PENDING";

                facility.setIsVerified(true);
                facility.setIsActive(true);
                facilityRepository.save(facility);

                // Log audit
                auditLogService.logAction(
                                "FACILITY_APPROVAL",
                                "Approved facility " + facilityId + ". Notes: " + notes);

                // Send email notification
                if (facility.getEmail() != null) {
                        emailService.sendFacilityApprovalEmail(facility.getEmail(), facility.getName(), true, notes);
                }

                log.info("Facility {} approved successfully", facilityId);
        }

        /**
         * Reject a recycling facility
         */
        @Transactional
        public void rejectFacility(UUID facilityId, UUID adminUserId, String reason) {
                log.info("Admin {} rejecting facility {}", adminUserId, facilityId);

                RecyclingFacility facility = facilityRepository.findById(facilityId)
                                .orElseThrow(() -> new IllegalArgumentException("Facility not found: " + facilityId));

                String oldStatus = facility.getIsVerified() != null && facility.getIsVerified() ? "APPROVED"
                                : "PENDING";

                facility.setIsVerified(false);
                facility.setIsActive(false);
                facilityRepository.save(facility);

                // Log audit
                auditLogService.logAction(
                                "FACILITY_REJECTION",
                                "Rejected facility " + facilityId + ". Reason: " + reason);

                // Send email notification
                if (facility.getEmail() != null) {
                        emailService.sendFacilityApprovalEmail(facility.getEmail(), facility.getName(), false, reason);
                }

                log.info("Facility {} rejected", facilityId);
        }

        /**
         * Override device model pricing
         */
        @Transactional
        public void overrideModelPrice(UUID modelId, AdminPriceOverrideRequest request, UUID adminUserId) {
                log.info("Admin {} overriding price for model {}", adminUserId, modelId);

                DeviceModel model = deviceModelRepository.findById(modelId)
                                .orElseThrow(() -> new IllegalArgumentException("Device model not found: " + modelId));

                String oldValue = model.getManualPriceOverride() != null ? model.getManualPriceOverride().toString()
                                : "null";

                model.setManualPriceOverride(request.getOverridePrice());
                model.setPriceOverrideReason(request.getReason());
                model.setPriceOverrideBy(adminUserId);
                model.setPriceOverrideAt(LocalDateTime.now());
                deviceModelRepository.save(model);

                // Log audit
                auditLogService.logAction(
                                "PRICE_OVERRIDE",
                                "Overrode price for model " + modelId + " from " + oldValue + " to "
                                                + request.getOverridePrice() + ". Reason: " + request.getReason());

                log.info("Price override applied to model {}: {}", modelId, request.getOverridePrice());
        }

        /**
         * Reassign recycle request to different facility
         */
        @Transactional
        public void reassignFacility(UUID requestId, FacilityReassignmentRequest request, UUID adminUserId) {
                log.info("Admin {} reassigning request {} to facility {}",
                                adminUserId, requestId, request.getNewFacilityId());

                RecycleRequest recycleRequest = recycleRequestRepository.findById(requestId)
                                .orElseThrow(() -> new RecycleRequestNotFoundException(requestId));

                // Validate status
                if (recycleRequest.getStatus() != RecycleStatus.CREATED &&
                                recycleRequest.getStatus() != RecycleStatus.APPROVED) {
                        throw new IllegalStateException(
                                        "Can only reassign requests in CREATED or APPROVED status. Current status: " +
                                                        recycleRequest.getStatus());
                }

                // Validate new facility
                RecyclingFacility newFacility = facilityRepository.findById(request.getNewFacilityId())
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Facility not found: " + request.getNewFacilityId()));

                if (!Boolean.TRUE.equals(newFacility.getIsVerified())
                                || !Boolean.TRUE.equals(newFacility.getIsActive())) {
                        throw new IllegalStateException("New facility must be approved and active");
                }

                recycleRequest.setRecyclingFacility(newFacility);
                recycleRequestRepository.save(recycleRequest);

                // Log audit
                auditLogService.logAction(
                                "FACILITY_REASSIGNMENT",
                                "Reassigned request " + requestId + " to facility " + request.getNewFacilityId()
                                                + ". Reason: " + request.getReason());

                // Send email notifications
                if (newFacility.getEmail() != null) {
                        emailService.sendFacilityReassignmentEmail(
                                        newFacility.getEmail(),
                                        recycleRequest.getId().toString(),
                                        true);
                }

                log.info("Request {} reassigned to facility {}", requestId, request.getNewFacilityId());
        }

        /**
         * Change pricing on recycle request
         */
        @Transactional
        public void changePricing(UUID requestId, AdminPriceChangeRequest request, UUID adminUserId) {
                log.info("Admin {} changing pricing for request {}", adminUserId, requestId);

                RecycleRequest recycleRequest = recycleRequestRepository.findById(requestId)
                                .orElseThrow(() -> new RecycleRequestNotFoundException(requestId));

                String oldEstimated = recycleRequest.getEstimatedAmount() != null
                                ? recycleRequest.getEstimatedAmount().toString()
                                : "null";
                String oldFinal = recycleRequest.getFinalAmount() != null ? recycleRequest.getFinalAmount().toString()
                                : "null";

                // Update estimated amount if provided
                if (request.getEstimatedAmount() != null) {
                        recycleRequest.setEstimatedAmount(request.getEstimatedAmount());

                        auditLogService.logAction(
                                        "PRICE_CHANGE",
                                        "Updated estimated amount on request " + requestId + " from " + oldEstimated
                                                        + " to " + request.getEstimatedAmount() + ". Reason: "
                                                        + request.getReason());
                }

                // Update final amount if provided
                if (request.getFinalAmount() != null) {
                        // If request is already recycled and wallet was credited, adjust wallet
                        if (recycleRequest.getStatus() == RecycleStatus.RECYCLED &&
                                        recycleRequest.getFinalAmount() != null) {

                                walletService.adjustWalletForPriceChange(
                                                recycleRequest.getUserId(),
                                                recycleRequest,
                                                recycleRequest.getFinalAmount(),
                                                request.getFinalAmount(),
                                                request.getReason());
                        }

                        recycleRequest.setFinalAmount(request.getFinalAmount());

                        auditLogService.logAction(
                                        "PRICE_CHANGE",
                                        "Updated final amount on request " + requestId + " from " + oldFinal + " to "
                                                        + request.getFinalAmount() + ". Reason: "
                                                        + request.getReason());
                }

                recycleRequestRepository.save(recycleRequest);

                // Send email notification to citizen
                User user = userRepository.findById(recycleRequest.getUserId())
                                .orElse(null);
                if (user != null && user.getEmail() != null) {
                        emailService.sendPriceChangeNotification(
                                        user.getEmail(),
                                        recycleRequest.getId().toString(),
                                        request.getReason());
                }

                log.info("Pricing updated for request {}", requestId);
        }

        /**
         * Activate or deactivate citizen account
         */
        @Transactional
        public void toggleCitizenActive(UUID citizenId, boolean active, UUID adminUserId, String reason) {
                log.info("Admin {} {} citizen {}", adminUserId, active ? "activating" : "deactivating", citizenId);

                User citizen = userRepository.findById(citizenId)
                                .orElseThrow(() -> new IllegalArgumentException("Citizen not found: " + citizenId));

                String oldValue = citizen.getIsActive() != null && citizen.getIsActive() ? "ACTIVE" : "INACTIVE";
                String newValue = active ? "ACTIVE" : "INACTIVE";

                citizen.setIsActive(active);
                userRepository.save(citizen);

                // Log audit
                auditLogService.logAction(
                                "CITIZEN_STATUS_CHANGE",
                                "Changed citizen " + citizenId + " status to " + newValue + ". Reason: " + reason);

                log.info("Citizen {} status changed to {}", citizenId, newValue);
        }
}
