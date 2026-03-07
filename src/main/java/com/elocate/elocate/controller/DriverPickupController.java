package com.elocate.elocate.controller;

import com.elocate.elocate.dto.DriverActionReasonDto;
import com.elocate.elocate.dto.RecycleRequestResponse;
import com.elocate.elocate.service.RecycleRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * Driver controller for pickup actions via email links
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/driver/pickup")
@RequiredArgsConstructor
public class DriverPickupController {

    private final RecycleRequestService recycleRequestService;

    /**
     * Mark pickup as completed (called from email link)
     */
    @GetMapping("/{requestId}/complete")
    public ResponseEntity<Map<String, String>> completePickup(
            @PathVariable UUID requestId,
            @RequestParam String token) {
        
        log.info("Driver marking pickup as complete for request: {}", requestId);
        
        // TODO: Validate token (JWT or database token)
        // For now, we'll proceed without validation
        
        RecycleRequestResponse response = recycleRequestService.handleDriverPickupDone(
                requestId, token);
        
        return ResponseEntity.ok(Map.of(
                "message", "Pickup marked as completed successfully",
                "requestId", requestId.toString(),
                "status", response.getFulfillmentStatus().toString()
        ));
    }

    /**
     * Mark pickup as failed (called from email link)
     */
    @PostMapping("/{requestId}/fail")
    public ResponseEntity<Map<String, String>> failPickup(
            @PathVariable UUID requestId,
            @RequestParam String token,
            @Valid @RequestBody DriverActionReasonDto reasonDto) {
        
        log.info("Driver marking pickup as failed for request: {}", requestId);
        
        // TODO: Validate token (JWT or database token)
        // For now, we'll proceed without validation
        
        RecycleRequestResponse response = recycleRequestService.handleDriverPickupFailed(
                requestId, reasonDto);
        
        return ResponseEntity.ok(Map.of(
                "message", "Pickup marked as failed",
                "requestId", requestId.toString(),
                "status", response.getFulfillmentStatus().toString()
        ));
    }
}
