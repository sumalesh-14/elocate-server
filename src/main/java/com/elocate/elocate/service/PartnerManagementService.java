package com.elocate.elocate.service;

import com.elocate.elocate.dto.*;
import com.elocate.elocate.exception.ResourceNotFoundException;
import com.elocate.elocate.model.RecyclingFacility;
import com.elocate.elocate.model.User;
import com.elocate.elocate.model.enums.OtpType;
import com.elocate.elocate.repository.RecycleRequestRepository;
import com.elocate.elocate.repository.RecyclingFacilityRepository;
import com.elocate.elocate.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class PartnerManagementService {

    private final RecyclingFacilityRepository facilityRepository;
    private final UserRepository userRepository;
    private final RecycleRequestRepository recycleRequestRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final AdminAuditLogService adminAuditLogService;
    private final EmailService emailService;
    private final Auth0Service auth0Service;

    @Transactional
    public PartnerResponse onboardPartner(PartnerOnboardingRequest request) {
        log.info("Onboarding new partner with registration number: {}", request.getRegistrationNumber());

        // Check if registration number already exists
        Optional<RecyclingFacility> existing = facilityRepository
                .findByRegistrationNumber(request.getRegistrationNumber());

        if (existing.isPresent()) {
            throw new IllegalArgumentException(
                    "Partner with registration number " + request.getRegistrationNumber() + " already exists");
        }

        RecyclingFacility facility = RecyclingFacility.builder()
                .registrationNumber(request.getRegistrationNumber())
                .name(request.getName())
                .address(request.getAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .capacity(request.getCapacity() != null ? request.getCapacity() : 1000)
                .contactNumber(request.getContactNumber())
                .operatingHours(request.getOperatingHours() != null ? request.getOperatingHours() : "9AM-6PM")
                .email(request.getEmail())
                .state(request.getState())
                .pincode(request.getPincode())
                .isVerified(false)
                .isActive(true)
                .build();

        RecyclingFacility saved = facilityRepository.save(facility);
        log.info("Partner onboarded successfully with ID: {}", saved.getId());

        return mapToResponse(saved);
    }

    public Page<PartnerResponse> listPartners(int page, int size, String search, Boolean isVerified) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<RecyclingFacility> facilities;

        if (search != null && !search.trim().isEmpty()) {
            if (isVerified != null) {
                facilities = facilityRepository
                        .findByNameContainingIgnoreCaseOrRegistrationNumberContainingIgnoreCaseAndIsVerified(
                                search, search, isVerified, pageRequest);
            } else {
                facilities = facilityRepository
                        .findByNameContainingIgnoreCaseOrRegistrationNumberContainingIgnoreCase(
                                search, search, pageRequest);
            }
        } else if (isVerified != null) {
            facilities = facilityRepository.findByIsVerified(isVerified, pageRequest);
        } else {
            facilities = facilityRepository.findAll(pageRequest);
        }

        return facilities.map(this::mapToResponse);
    }

    public PartnerResponse getPartnerById(UUID id) {
        RecyclingFacility facility = facilityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found with ID: " + id));
        return mapToResponse(facility);
    }

    public PartnerResponse getPartnerByRegistrationNumber(String registrationNumber) {
        RecyclingFacility facility = facilityRepository.findByRegistrationNumber(registrationNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Partner not found with registration number: " + registrationNumber));
        return mapToResponse(facility);
    }

    @Transactional
    public PartnerResponse verifyPartner(UUID id, PartnerVerificationRequest request) {
        log.info("Verifying partner with ID: {}", id);

        RecyclingFacility facility = facilityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found with ID: " + id));

        facility.setIsVerified(request.getIsVerified());
        RecyclingFacility updated = facilityRepository.save(facility);

        log.info("Partner {} verification status updated to: {}", id, request.getIsVerified());
        return mapToResponse(updated);
    }

    @Transactional
    public PartnerResponse updatePartner(UUID id, PartnerOnboardingRequest request) {
        log.info("Updating partner with ID: {}", id);

        RecyclingFacility facility = facilityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found with ID: " + id));

        // Check if registration number is being changed and if it conflicts
        if (!facility.getRegistrationNumber().equals(request.getRegistrationNumber())) {
            Optional<RecyclingFacility> existing = facilityRepository
                    .findByRegistrationNumber(request.getRegistrationNumber());
            if (existing.isPresent()) {
                throw new IllegalArgumentException(
                        "Partner with registration number " + request.getRegistrationNumber() + " already exists");
            }
        }

        facility.setRegistrationNumber(request.getRegistrationNumber());
        facility.setName(request.getName());
        facility.setAddress(request.getAddress());
        facility.setLatitude(request.getLatitude());
        facility.setLongitude(request.getLongitude());
        facility.setCapacity(request.getCapacity());
        facility.setContactNumber(request.getContactNumber());
        facility.setOperatingHours(request.getOperatingHours());
        facility.setEmail(request.getEmail());
        facility.setState(request.getState());
        facility.setPincode(request.getPincode());

        RecyclingFacility updated = facilityRepository.save(facility);
        log.info("Partner updated successfully: {}", id);

        return mapToResponse(updated);
    }

    @Transactional
    public void deletePartner(UUID id) {
        log.info("Deleting partner with ID: {}", id);

        if (!facilityRepository.existsById(id)) {
            throw new ResourceNotFoundException("Partner not found with ID: " + id);
        }

        facilityRepository.deleteById(id);
        log.info("Partner deleted successfully: {}", id);
    }

    private PartnerResponse mapToResponse(RecyclingFacility facility) {
        return PartnerResponse.builder()
                .id(facility.getId())
                .registrationNumber(facility.getRegistrationNumber())
                .name(facility.getName())
                .address(facility.getAddress())
                .latitude(facility.getLatitude())
                .longitude(facility.getLongitude())
                .capacity(facility.getCapacity())
                .contactNumber(facility.getContactNumber())
                .operatingHours(facility.getOperatingHours())
                .email(facility.getEmail())
                .state(facility.getState())
                .pincode(facility.getPincode())
                .isVerified(facility.getIsVerified())
                .isActive(facility.getIsActive())
                .fullName(facility.getUser() != null ? facility.getUser().getFullName() : "N/A")
                .createdAt(facility.getCreatedAt())
                .updatedAt(facility.getUpdatedAt())
                .build();
    }

    /**
     * Partner self-registration with user credentials
     * Status: PENDING (requires admin approval)
     */
    @Transactional
    public PartnerResponse registerPartner(PartnerRegistrationRequest request) {
        log.info("Partner self-registration for: {}", request.getEmail());

        // Check if email already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }

        // Check if mobile number already exists
        if (userRepository.existsByMobileNumber(request.getMobileNumber())) {
            throw new IllegalArgumentException("Mobile number already registered");
        }

        // Check if registration number already exists
        if (facilityRepository.findByRegistrationNumber(request.getRegistrationNumber()).isPresent()) {
            throw new IllegalArgumentException("Registration number already exists");
        }

        // Create user in Auth0 first
        String firebaseUid;
        try {
            firebaseUid = auth0Service.createUser(request.getEmail(), request.getPassword(), request.getEmail());
            log.info("Created Auth0 user for partner with ID: {}", firebaseUid);
        } catch (Exception e) {
            log.error("Failed to create Auth0 user for partner: {}", e.getMessage());
            if (e.getMessage().contains("User already exists") || e.getMessage().contains("email_exists")
                    || e.getMessage().contains("already exists")) {
                throw new IllegalArgumentException("Email already registered in Auth0");
            }
            throw new RuntimeException("Auth0 registration failed: " + e.getMessage());
        }

        // Create user account in database
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .mobileNumber(request.getMobileNumber())
                .passwordHash("AUTH0_MANAGED") // Password managed by Auth0
                .firebaseUid(firebaseUid)
                .role("PARTNER")
                .isEmailVerified(false)
                .isActive(false) // Inactive until approved
                .build();

        user = userRepository.save(user);

        // Create facility
        RecyclingFacility facility = RecyclingFacility.builder()
                .user(user)
                .registrationNumber(request.getRegistrationNumber())
                .name(request.getFacilityName())
                .address(request.getAddress())
                .latitude(BigDecimal.valueOf(request.getLatitude()))
                .longitude(BigDecimal.valueOf(request.getLongitude()))
                .capacity(request.getCapacity() != null ? request.getCapacity() : 1000)
                .contactNumber(request.getContactNumber())
                .operatingHours(request.getOperatingHours() != null ? request.getOperatingHours() : "9AM-6PM")
                .email(request.getEmail())
                .state(request.getState())
                .pincode(request.getPincode())
                .approvalStatus("PENDING")
                .isVerified(false)
                .isActive(false)
                .build();

        facility = facilityRepository.save(facility);
        log.info("Partner registered successfully. Awaiting approval. User ID: {}, Facility ID: {}",
                user.getId(), facility.getId());

        // Generate and send OTP for email verification
        otpService.generateAndSendOtp(request.getEmail(), OtpType.EMAIL_VERIFICATION);
        log.info("OTP generated for partner email verification: {}", request.getEmail());

        return mapToResponse(facility);
    }

    /**
     * Admin creates partner account directly
     * Status: APPROVED (auto-approved)
     */
    @Transactional
    public PartnerResponse adminCreatePartner(AdminCreatePartnerRequest request) {
        log.info("Admin creating partner account for: {}", request.getEmail());

        // Check if email already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }

        // Check if mobile number already exists
        if (userRepository.existsByMobileNumber(request.getMobileNumber())) {
            throw new IllegalArgumentException("Mobile number already registered");
        }

        // Check if registration number already exists
        if (facilityRepository.findByRegistrationNumber(request.getRegistrationNumber()).isPresent()) {
            throw new IllegalArgumentException("Registration number already exists");
        }

        // Create user in Auth0 first
        String firebaseUid;
        try {
            firebaseUid = auth0Service.createUser(request.getEmail(), request.getTemporaryPassword(), request.getEmail());
            log.info("Created Auth0 user for admin-created partner with ID: {}", firebaseUid);
        } catch (Exception e) {
            log.error("Failed to create Auth0 user for admin-created partner: {}", e.getMessage());
            if (e.getMessage().contains("User already exists") || e.getMessage().contains("email_exists")
                    || e.getMessage().contains("already exists")) {
                throw new IllegalArgumentException("Email already registered in Auth0");
            }
            throw new RuntimeException("Auth0 registration failed: " + e.getMessage());
        }

        // Create user account in database
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .mobileNumber(request.getMobileNumber())
                .passwordHash("AUTH0_MANAGED") // Password managed by Auth0
                .firebaseUid(firebaseUid)
                .role("PARTNER")
                .isEmailVerified(true)
                .isActive(true) // Active immediately
                .build();

        user = userRepository.save(user);

        // Create facility
        RecyclingFacility facility = RecyclingFacility.builder()
                .user(user)
                .registrationNumber(request.getRegistrationNumber())
                .name(request.getFacilityName())
                .address(request.getAddress())
                .latitude(BigDecimal.valueOf(request.getLatitude()))
                .longitude(BigDecimal.valueOf(request.getLongitude()))
                .capacity(request.getCapacity() != null ? request.getCapacity() : 1000)
                .contactNumber(request.getContactNumber())
                .operatingHours(request.getOperatingHours() != null ? request.getOperatingHours() : "9AM-6PM")
                .email(request.getEmail())
                .state(request.getState())
                .pincode(request.getPincode())
                .approvalStatus(request.getAutoApprove() ? "APPROVED" : "PENDING")
                .isVerified(request.getAutoApprove())
                .isActive(request.getAutoApprove())
                .build();

        facility = facilityRepository.save(facility);
        log.info("Partner created by admin. User ID: {}, Facility ID: {}", user.getId(), facility.getId());

        return mapToResponse(facility);
    }

    /**
     * Approve or reject partner registration
     */
    @Transactional
    public PartnerResponse approvePartner(UUID facilityId, PartnerApprovalRequest request) {
        log.info("Processing partner approval for facility: {}", facilityId);

        RecyclingFacility facility = facilityRepository.findById(facilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Facility not found"));

        User user = facility.getUser();
        if (user == null) {
            throw new IllegalStateException("No user associated with this facility");
        }

        facility.setApprovalStatus(request.getApprovalStatus());

        if ("APPROVED".equals(request.getApprovalStatus())) {
            facility.setIsVerified(request.getIsVerified());
            facility.setIsActive(true);
            user.setIsActive(true);
            user.setIsEmailVerified(true);
            userRepository.save(user);
            log.info("Partner approved and activated: {}", facilityId);
            
            // Send approval email notification
            try {
                emailService.sendPartnerApprovedEmail(
                    user.getEmail(),
                    user.getFullName(),
                    facility.getName(),
                    facility.getRegistrationNumber(),
                    request.getRemarks()
                );
                log.info("Approval email sent to partner: {}", user.getEmail());
            } catch (Exception e) {
                log.error("Failed to send approval email to partner: {}", user.getEmail(), e);
            }
            
            try {
                adminAuditLogService.logAction("APPROVE_PARTNER",
                        "Approved partner registration for facility: " + facility.getName());
            } catch (Exception e) {
            }
        } else if ("REJECTED".equals(request.getApprovalStatus())) {
            facility.setIsActive(false);
            user.setIsActive(false);
            userRepository.save(user);
            log.info("Partner rejected: {}", facilityId);
            
            // Send rejection email notification
            try {
                emailService.sendPartnerRejectedEmail(
                    user.getEmail(),
                    user.getFullName(),
                    facility.getName(),
                    facility.getRegistrationNumber(),
                    request.getRemarks()
                );
                log.info("Rejection email sent to partner: {}", user.getEmail());
            } catch (Exception e) {
                log.error("Failed to send rejection email to partner: {}", user.getEmail(), e);
            }
            
            try {
                adminAuditLogService.logAction("REJECT_PARTNER",
                        "Rejected partner registration for facility: " + facility.getName());
            } catch (Exception e) {
            }
        }

        facility = facilityRepository.save(facility);
        return mapToResponse(facility);
    }

    /**
     * Get partner dashboard data
     */
    public PartnerDashboardResponse getPartnerDashboard(UUID userId) {
        log.info("Fetching partner dashboard for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        RecyclingFacility facility = facilityRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("No facility associated with this user"));

        // Get statistics
        Long totalRequests = recycleRequestRepository.countByRecyclingFacility(facility);
        Long pendingRequests = recycleRequestRepository.countByRecyclingFacilityAndStatus(facility, "CREATED");
        Long completedRequests = recycleRequestRepository.countByRecyclingFacilityAndStatus(facility, "RECYCLED");
        Long rejectedRequests = recycleRequestRepository.countByRecyclingFacilityAndStatus(facility, "REJECTED");

        return PartnerDashboardResponse.builder()
                .facilityId(facility.getId())
                .registrationNumber(facility.getRegistrationNumber())
                .facilityName(facility.getName())
                .address(facility.getAddress())
                .latitude(facility.getLatitude())
                .longitude(facility.getLongitude())
                .capacity(facility.getCapacity())
                .contactNumber(facility.getContactNumber())
                .operatingHours(facility.getOperatingHours())
                .email(facility.getEmail())
                .state(facility.getState())
                .pincode(facility.getPincode())
                .approvalStatus(facility.getApprovalStatus())
                .isVerified(facility.getIsVerified())
                .isActive(facility.getIsActive())
                .createdAt(facility.getCreatedAt())
                .userId(user.getId())
                .fullName(user.getFullName())
                .mobileNumber(user.getMobileNumber())
                .totalRequests(totalRequests)
                .pendingRequests(pendingRequests)
                .completedRequests(completedRequests)
                .rejectedRequests(rejectedRequests)
                .build();
    }

    /**
     * Partner updates their own facility
     */
    @Transactional
    public PartnerResponse updateOwnFacility(UUID userId, PartnerOnboardingRequest request) {
        log.info("Partner updating own facility. User: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        RecyclingFacility facility = facilityRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("No facility associated with this user"));

        // Check if registration number is being changed
        if (!facility.getRegistrationNumber().equals(request.getRegistrationNumber())) {
            if (facilityRepository.findByRegistrationNumber(request.getRegistrationNumber()).isPresent()) {
                throw new IllegalArgumentException("Registration number already exists");
            }
        }

        facility.setRegistrationNumber(request.getRegistrationNumber());
        facility.setName(request.getName());
        facility.setAddress(request.getAddress());
        facility.setLatitude(request.getLatitude());
        facility.setLongitude(request.getLongitude());
        facility.setCapacity(request.getCapacity());
        facility.setContactNumber(request.getContactNumber());
        facility.setOperatingHours(request.getOperatingHours());
        facility.setEmail(request.getEmail());
        facility.setState(request.getState());
        facility.setPincode(request.getPincode());

        facility = facilityRepository.save(facility);
        log.info("Partner facility updated: {}", facility.getId());

        return mapToResponse(facility);
    }

    /**
     * List partners by approval status
     */
    public Page<PartnerResponse> listPartnersByStatus(String status, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<RecyclingFacility> facilities = facilityRepository.findByApprovalStatus(status, pageRequest);
        return facilities.map(this::mapToResponse);
    }
}
