package com.elocate.elocate.controller;

import com.elocate.elocate.dto.*;
import com.elocate.elocate.service.PartnerManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.elocate.elocate.context.UserContextHolder;
import java.util.UUID;

/**
 * Controller for partner authentication and self-service operations
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/partner-auth")
@RequiredArgsConstructor
public class PartnerAuthController {
    
    private final PartnerManagementService partnerManagementService;
    
    /**
     * Partner self-registration (requires admin approval)
     */
    @PostMapping("/register")
    public ResponseEntity<PartnerResponse> registerPartner(
            @Valid @RequestBody PartnerRegistrationRequest request) {
        System.out.println("=== PARTNER REGISTRATION BACKEND ===");
        System.out.println("Endpoint: POST /api/v1/partner-auth/register");
        System.out.println("Request: " + request);
        System.out.println("Email: " + request.getEmail());
        System.out.println("Facility Name: " + request.getFacilityName());
        System.out.println("Registration Number: " + request.getRegistrationNumber());
        System.out.println("===================================");
        
        log.info("Partner self-registration request for: {}", request.getEmail());
        PartnerResponse response = partnerManagementService.registerPartner(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Get partner dashboard (authenticated partner)
     */
    @GetMapping("/dashboard")
    public ResponseEntity<PartnerDashboardResponse> getDashboard() {
        UUID userId = UserContextHolder.getContext().getUserId();
        log.info("Fetching dashboard for partner user: {}", userId);
        PartnerDashboardResponse dashboard = partnerManagementService.getPartnerDashboard(userId);
        return ResponseEntity.ok(dashboard);
    }
    
    /**
     * Partner updates their own facility
     */
    @PutMapping("/facility")
    public ResponseEntity<PartnerResponse> updateOwnFacility(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody PartnerOnboardingRequest request) {
        log.info("Partner updating own facility. User: {}", userId);
        PartnerResponse response = partnerManagementService.updateOwnFacility(userId, request);
        return ResponseEntity.ok(response);
    }
}
