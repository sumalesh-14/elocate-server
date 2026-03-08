package com.elocate.elocate.service;

import com.elocate.elocate.dto.*;
import com.elocate.elocate.model.*;
import com.elocate.elocate.model.enums.FulfillmentStatus;
import com.elocate.elocate.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service for driver pickup actions via email links
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DriverPickupService {

    private final DriverPickupTokenRepository tokenRepository;
    private final RecycleRequestRepository recycleRequestRepository;
    private final DriverRepository driverRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;
    private final RecycleStatusHistoryService statusHistoryService;
    private final EmailService emailService;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    /**
     * Generate tokens and send email to driver
     */
    @Transactional
    public void generateTokensAndSendEmail(UUID requestId, UUID driverId, String assignmentComments) {
        RecycleRequest request = recycleRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new IllegalArgumentException("Driver not found"));

        User citizen = userRepository.findById(request.getUserId()).orElse(null);
        
        // Get pickup address
        String citizenAddress = "Address not available";
        if (request.getPickupAddress() != null) {
            UserAddress address = request.getPickupAddress();
            citizenAddress = String.format("%s, %s, %s %s", 
                address.getAddress(), address.getCity(), address.getState(), address.getPincode());
        }
        
        // Get device name from model
        String deviceName = request.getDeviceModel() != null ? 
            request.getDeviceModel().getModelName() : "Unknown Device";

        // Generate tokens (24 hour expiry)
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(24);
        
        String acceptToken = UUID.randomUUID().toString();
        String rejectToken = UUID.randomUUID().toString();

        // Save tokens
        DriverPickupToken acceptTokenEntity = DriverPickupToken.builder()
                .recycleRequestId(requestId)
                .driverId(driverId)
                .token(acceptToken)
                .actionType("ACCEPT")
                .expiresAt(expiresAt)
                .build();

        DriverPickupToken rejectTokenEntity = DriverPickupToken.builder()
                .recycleRequestId(requestId)
                .driverId(driverId)
                .token(rejectToken)
                .actionType("REJECT")
                .expiresAt(expiresAt)
                .build();

        tokenRepository.save(acceptTokenEntity);
        tokenRepository.save(rejectTokenEntity);

        // Send email with frontend links and assignment comments
        String acceptLink = frontendUrl + "/driver/pickup/accept/" + acceptToken;
        String rejectLink = frontendUrl + "/driver/pickup/reject/" + rejectToken;

        sendDriverPickupEmail(driver.getEmail(), driver.getName(), requestId.toString(), 
                citizenAddress, deviceName, acceptLink, rejectLink, assignmentComments);

        log.info("✅ Pickup tokens generated and email sent to driver: {}", driver.getEmail());
    }

    /**
     * Validate token
     */
    public TokenValidationResponse validateToken(String token) {
        DriverPickupToken tokenEntity = tokenRepository.findByToken(token).orElse(null);

        if (tokenEntity == null) {
            return TokenValidationResponse.builder()
                    .valid(false)
                    .message("Invalid token")
                    .build();
        }

        if (!tokenEntity.isValid()) {
            String message = tokenEntity.getUsed() ? "Token already used" : "Token expired";
            return TokenValidationResponse.builder()
                    .valid(false)
                    .message(message)
                    .build();
        }

        // Get request and driver details
        RecycleRequest request = recycleRequestRepository.findById(tokenEntity.getRecycleRequestId()).orElse(null);
        Driver driver = driverRepository.findById(tokenEntity.getDriverId()).orElse(null);
        User citizen = request != null ? userRepository.findById(request.getUserId()).orElse(null) : null;
        
        // Get address
        String citizenAddress = "Unknown";
        if (request != null && request.getPickupAddress() != null) {
            UserAddress address = request.getPickupAddress();
            citizenAddress = String.format("%s, %s, %s %s", 
                address.getAddress(), address.getCity(), address.getState(), address.getPincode());
        }
        
        // Get device name
        String deviceName = "Unknown";
        if (request != null && request.getDeviceModel() != null) {
            deviceName = request.getDeviceModel().getModelName();
        }

        return TokenValidationResponse.builder()
                .valid(true)
                .action(tokenEntity.getActionType())
                .requestId(tokenEntity.getRecycleRequestId().toString())
                .driverName(driver != null ? driver.getName() : "Unknown")
                .citizenAddress(citizenAddress)
                .deviceName(deviceName)
                .message("Token is valid")
                .build();
    }

    /**
     * Accept pickup with photo and comments
     */
    @Transactional
    public void acceptPickup(String token, DriverPickupActionDto dto) {
        DriverPickupToken tokenEntity = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));

        if (!tokenEntity.isValid()) {
            throw new IllegalArgumentException(tokenEntity.getUsed() ? "Token already used" : "Token expired");
        }

        if (!"ACCEPT".equals(tokenEntity.getActionType())) {
            throw new IllegalArgumentException("Invalid token type for this action");
        }

        RecycleRequest request = recycleRequestRepository.findById(tokenEntity.getRecycleRequestId())
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));

        // Update request
        FulfillmentStatus oldStatus = request.getFulfillmentStatus();
        request.setFulfillmentStatus(FulfillmentStatus.PICKUP_COMPLETED);
        request.setPickupPhotoUrl(dto.getPhotoUrl());
        request.setDriverComments(dto.getComments());
        
        recycleRequestRepository.save(request);

        // Mark token as used
        tokenEntity.setUsed(true);
        tokenEntity.setUsedAt(LocalDateTime.now());
        tokenRepository.save(tokenEntity);

        // Record status change
        statusHistoryService.recordStatusChange(request, oldStatus, 
                FulfillmentStatus.PICKUP_COMPLETED, null, "Driver accepted pickup: " + dto.getComments());

        // Send notification to citizen
        User citizen = userRepository.findById(request.getUserId()).orElse(null);
        if (citizen != null && citizen.getEmail() != null) {
            emailService.sendPickupCompletedEmail(citizen.getEmail(), request.getId().toString());
        }

        log.info("✅ Pickup accepted for request: {}", request.getId());
    }

    /**
     * Reject pickup with reason
     */
    @Transactional
    public void rejectPickup(String token, DriverPickupRejectDto dto) {
        DriverPickupToken tokenEntity = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));

        if (!tokenEntity.isValid()) {
            throw new IllegalArgumentException(tokenEntity.getUsed() ? "Token already used" : "Token expired");
        }

        if (!"REJECT".equals(tokenEntity.getActionType())) {
            throw new IllegalArgumentException("Invalid token type for this action");
        }

        RecycleRequest request = recycleRequestRepository.findById(tokenEntity.getRecycleRequestId())
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));

        // Update request
        FulfillmentStatus oldStatus = request.getFulfillmentStatus();
        request.setFulfillmentStatus(FulfillmentStatus.PICKUP_FAILED);
        request.setDriverComments(dto.getReason());
        
        recycleRequestRepository.save(request);

        // Mark token as used
        tokenEntity.setUsed(true);
        tokenEntity.setUsedAt(LocalDateTime.now());
        tokenRepository.save(tokenEntity);

        // Record status change
        statusHistoryService.recordStatusChange(request, oldStatus, 
                FulfillmentStatus.PICKUP_FAILED, null, "Driver rejected pickup: " + dto.getReason());

        // Send notification to citizen
        User citizen = userRepository.findById(request.getUserId()).orElse(null);
        if (citizen != null && citizen.getEmail() != null) {
            emailService.sendPickupFailedEmail(citizen.getEmail(), request.getId().toString(), dto.getReason());
        }

        log.info("✅ Pickup rejected for request: {}", request.getId());
    }

    /**
     * Generate pre-signed URL for photo upload
     */
    public PresignedUrlResponse generateUploadUrl(String token) {
        DriverPickupToken tokenEntity = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));

        if (!tokenEntity.isValid()) {
            throw new IllegalArgumentException("Token expired or already used");
        }

        String photoKey = s3Service.generatePickupPhotoKey(tokenEntity.getRecycleRequestId());
        // Use generic image/* content type to accept any image format
        String uploadUrl = s3Service.generatePresignedUploadUrl(photoKey, "image/jpeg", 15); // 15 minutes
        String publicUrl = s3Service.getPublicUrl(photoKey);

        return PresignedUrlResponse.builder()
                .uploadUrl(uploadUrl)
                .photoKey(photoKey)
                .publicUrl(publicUrl)
                .expiresInMinutes(15)
                .build();
    }

    /**
     * Send driver pickup email
     */
    private void sendDriverPickupEmail(String toEmail, String driverName, String requestId,
                                       String address, String deviceName, String acceptLink, 
                                       String rejectLink, String assignmentComments) {
        // Extract token from accept link
        String pickupToken = acceptLink.substring(acceptLink.lastIndexOf("/") + 1);
        
        // Use the new email service method with HTML templates
        emailService.sendDriverAssignmentEmailWithComments(
            toEmail, 
            driverName, 
            requestId, 
            address, 
            pickupToken,
            assignmentComments,
            deviceName,
            "As scheduled"
        );
    }

    /**
     * Invalidate all active tokens for a request (used during driver reassignment)
     */
    @Transactional
    public int invalidateTokensForRequest(UUID requestId) {
        int count = tokenRepository.invalidateTokensForRequest(requestId);
        log.info("Invalidated {} tokens for request {}", count, requestId);
        return count;
    }

    /**
     * Check if request has SLA breach (more than 24 hours since driver assignment)
     */
    public boolean hasSLABreach(RecycleRequest request) {
        if (request.getAssignedDriverId() == null) {
            return false;
        }
        
        if (request.getFulfillmentStatus() != FulfillmentStatus.PICKUP_ASSIGNED) {
            return false;
        }

        LocalDateTime assignedAt = request.getUpdatedAt();
        LocalDateTime slaDeadline = assignedAt.plusHours(24);
        
        return LocalDateTime.now().isAfter(slaDeadline);
    }
}
