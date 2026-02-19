package com.elocate.elocate.controller;

import com.elocate.elocate.dto.*;
import com.elocate.elocate.service.PartnerManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/partners")
@RequiredArgsConstructor
public class PartnerManagementController {
    
    private final PartnerManagementService partnerManagementService;
    
    /**
     * Admin creates partner account directly with credentials
     */
    @PostMapping("/admin/create")
    public ResponseEntity<PartnerResponse> adminCreatePartner(
            @Valid @RequestBody AdminCreatePartnerRequest request) {
        log.info("Admin creating partner account for: {}", request.getEmail());
        PartnerResponse response = partnerManagementService.adminCreatePartner(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Admin approves or rejects partner registration
     */
    @PatchMapping("/{id}/approve")
    public ResponseEntity<PartnerResponse> approvePartner(
            @PathVariable UUID id,
            @Valid @RequestBody PartnerApprovalRequest request) {
        log.info("Admin processing approval for partner: {}", id);
        PartnerResponse response = partnerManagementService.approvePartner(id, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * List partners by approval status
     */
    @GetMapping("/by-status/{status}")
    public ResponseEntity<Page<PartnerResponse>> listPartnersByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Listing partners with status: {}", status);
        Page<PartnerResponse> partners = partnerManagementService.listPartnersByStatus(status, page, size);
        return ResponseEntity.ok(partners);
    }
    
    @PostMapping("/onboard")
    public ResponseEntity<PartnerResponse> onboardPartner(
            @Valid @RequestBody PartnerOnboardingRequest request) {
        log.info("Received partner onboarding request for: {}", request.getName());
        PartnerResponse response = partnerManagementService.onboardPartner(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping
    public ResponseEntity<Page<PartnerResponse>> listPartners(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isVerified) {
        log.info("Listing partners - page: {}, size: {}, search: {}, isVerified: {}", 
                page, size, search, isVerified);
        Page<PartnerResponse> partners = partnerManagementService.listPartners(page, size, search, isVerified);
        return ResponseEntity.ok(partners);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<PartnerResponse> getPartnerById(@PathVariable UUID id) {
        log.info("Fetching partner by ID: {}", id);
        PartnerResponse partner = partnerManagementService.getPartnerById(id);
        return ResponseEntity.ok(partner);
    }
    
    @GetMapping("/registration/{registrationNumber}")
    public ResponseEntity<PartnerResponse> getPartnerByRegistrationNumber(
            @PathVariable String registrationNumber) {
        log.info("Fetching partner by registration number: {}", registrationNumber);
        PartnerResponse partner = partnerManagementService.getPartnerByRegistrationNumber(registrationNumber);
        return ResponseEntity.ok(partner);
    }
    
    @PatchMapping("/{id}/verify")
    public ResponseEntity<PartnerResponse> verifyPartner(
            @PathVariable UUID id,
            @Valid @RequestBody PartnerVerificationRequest request) {
        log.info("Verifying partner: {}", id);
        PartnerResponse partner = partnerManagementService.verifyPartner(id, request);
        return ResponseEntity.ok(partner);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<PartnerResponse> updatePartner(
            @PathVariable UUID id,
            @Valid @RequestBody PartnerOnboardingRequest request) {
        log.info("Updating partner: {}", id);
        PartnerResponse partner = partnerManagementService.updatePartner(id, request);
        return ResponseEntity.ok(partner);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePartner(@PathVariable UUID id) {
        log.info("Deleting partner: {}", id);
        partnerManagementService.deletePartner(id);
        return ResponseEntity.noContent().build();
    }
}
