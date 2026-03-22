package com.elocate.elocate.controller;

import com.elocate.elocate.dto.*;
import com.elocate.elocate.service.DriverPickupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller for driver pickup actions via email links
 * These endpoints are publicly accessible (whitelisted in SecurityConfig)
 */
@RestController
@RequestMapping("/api/v1/driver/pickup")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Driver Pickup", description = "Driver pickup actions via email links (Public)")
public class DriverPickupActionController {

    private final DriverPickupService driverPickupService;

    /**
     * Validate token and get pickup details
     */
    @GetMapping("/validate/{token}")
    @Operation(summary = "Validate pickup token", description = "Validate token and get pickup details for frontend")
    public ResponseEntity<TokenValidationResponse> validateToken(@PathVariable String token) {
        log.info("🔵 [DRIVER API 1] GET /api/v1/driver/pickup/validate/{}", token);
        
        try {
            TokenValidationResponse response = driverPickupService.validateToken(token);
            log.info("✅ [DRIVER API 1] Token validation successful - Action: {}, RequestId: {}", 
                    response.getAction(), response.getRequestId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ [DRIVER API 1] Error validating token", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(TokenValidationResponse.builder()
                            .valid(false)
                            .message("Error validating token: " + e.getMessage())
                            .build());
        }
    }

    /**
     * Driver marks themselves as on the way (in-progress)
     */
    @PostMapping("/on-my-way/{token}")
    @Operation(summary = "Mark in progress", description = "Driver marks pickup as in-progress (on the way)")
    public ResponseEntity<?> markInProgress(@PathVariable String token) {
        log.info("POST /api/v1/driver/pickup/on-my-way/{}", token);
        try {
            driverPickupService.markInProgress(token);
            return ResponseEntity.ok(Map.of("success", true, "message", "Pickup marked as in-progress"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", "Failed to mark in-progress: " + e.getMessage()));
        }
    }

    /**
     * Accept pickup with photo and comments
     */
    @PostMapping("/accept/{token}")
    @Operation(summary = "Accept pickup", description = "Driver accepts pickup with photo and comments")
    public ResponseEntity<?> acceptPickup(
            @PathVariable String token,
            @Valid @RequestBody DriverPickupActionDto dto) {
        log.info("🔵 [DRIVER API 3] POST /api/v1/driver/pickup/accept/{} - PhotoURL: {}, Comments: {}", 
                token, dto.getPhotoUrl(), dto.getComments());
        
        try {
            driverPickupService.acceptPickup(token, dto);
            log.info("✅ [DRIVER API 3] Pickup accepted successfully");
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Pickup accepted successfully"
            ));
        } catch (IllegalArgumentException e) {
            log.error("❌ [DRIVER API 3] Invalid pickup acceptance", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "success", false,
                            "error", e.getMessage()
                    ));
        } catch (Exception e) {
            log.error("❌ [DRIVER API 3] Error accepting pickup", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "error", "Failed to accept pickup: " + e.getMessage()
                    ));
        }
    }

    /**
     * Reject pickup with reason
     */
    @PostMapping("/reject/{token}")
    @Operation(summary = "Reject pickup", description = "Driver rejects pickup with reason")
    public ResponseEntity<?> rejectPickup(
            @PathVariable String token,
            @Valid @RequestBody DriverPickupRejectDto dto) {
        log.info("POST /api/v1/driver/pickup/reject/{} - Reason: {}", token, dto.getReason());
        
        try {
            driverPickupService.rejectPickup(token, dto);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Pickup rejected successfully"
            ));
        } catch (IllegalArgumentException e) {
            log.error("Invalid pickup rejection", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "success", false,
                            "error", e.getMessage()
                    ));
        } catch (Exception e) {
            log.error("Error rejecting pickup", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "error", "Failed to reject pickup: " + e.getMessage()
                    ));
        }
    }

    /**
     * Generate pre-signed URL for photo upload
     */
    @GetMapping("/upload-url/{token}")
    @Operation(summary = "Get upload URL", description = "Get pre-signed S3 URL for photo upload")
    public ResponseEntity<?> getUploadUrl(@PathVariable String token) {
        log.info("🔵 [DRIVER API 2] GET /api/v1/driver/pickup/upload-url/{}", token);
        
        try {
            PresignedUrlResponse response = driverPickupService.generateUploadUrl(token);
            log.info("✅ [DRIVER API 2] Generated S3 upload URL - Public URL: {}", response.getPublicUrl());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("❌ [DRIVER API 2] Invalid token for upload URL", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "success", false,
                            "error", e.getMessage()
                    ));
        } catch (Exception e) {
            log.error("❌ [DRIVER API 2] Error generating upload URL", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "error", "Failed to generate upload URL: " + e.getMessage()
                    ));
        }
    }
}
