package com.elocate.elocate.controller;

import com.elocate.elocate.dto.DriverPickupActionDto;
import com.elocate.elocate.dto.DriverPickupRejectDto;
import com.elocate.elocate.model.DriverPickupToken;
import com.elocate.elocate.model.RecycleRequest;
import com.elocate.elocate.repository.DriverPickupTokenRepository;
import com.elocate.elocate.repository.RecycleRequestRepository;
import com.elocate.elocate.service.DriverPickupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

/**
 * Open internal endpoint called by elocate-whatsapp-bot to perform driver pickup actions.
 * Uses the SAME DriverPickupService as the email-link flow.
 * No secret validation — secured at network/infra level.
 */
@RestController
@RequestMapping("/internal/driver")
@RequiredArgsConstructor
@Slf4j
public class DriverWhatsAppActionController {

    private final DriverPickupService driverPickupService;
    private final RecycleRequestRepository recycleRequestRepository;
    private final DriverPickupTokenRepository tokenRepository;

    // ------------------------------------------------------------------
    // Resolve requestNumber → active token for a given action type
    // ------------------------------------------------------------------
    @GetMapping("/token/{requestNumber}/{actionType}")
    public ResponseEntity<Map<String, Object>> getToken(
            @PathVariable String requestNumber,
            @PathVariable String actionType) {

        log.info("Token lookup: requestNumber={} actionType={}", requestNumber, actionType);

        Optional<RecycleRequest> reqOpt = recycleRequestRepository.findByRequestNumber(requestNumber);
        if (reqOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Request not found"));
        }

        Optional<DriverPickupToken> tokenOpt = tokenRepository
                .findByRecycleRequestIdAndActionType(reqOpt.get().getId(), actionType.toUpperCase());

        if (tokenOpt.isEmpty() || !tokenOpt.get().isValid()) {
            return ResponseEntity.badRequest().body(Map.of("success", false,
                    "error", "No valid token found for action: " + actionType));
        }

        return ResponseEntity.ok(Map.of("success", true, "token", tokenOpt.get().getToken()));
    }

    // ------------------------------------------------------------------
    // Mark In Progress
    // ------------------------------------------------------------------
    @PostMapping("/in-progress/{token}")
    public ResponseEntity<Map<String, Object>> markInProgress(@PathVariable String token) {
        log.info("Mark in-progress: token={}", token);
        try {
            driverPickupService.markInProgress(token);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // ------------------------------------------------------------------
    // Accept Pickup
    // ------------------------------------------------------------------
    @PostMapping("/accept/{token}")
    public ResponseEntity<Map<String, Object>> acceptPickup(
            @PathVariable String token,
            @RequestBody(required = false) DriverPickupActionDto dto) {

        log.info("Accept pickup: token={}", token);
        if (dto == null) dto = new DriverPickupActionDto();
        if (dto.getComments() == null) dto.setComments("Accepted via Telegram");

        try {
            driverPickupService.acceptPickup(token, dto);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // ------------------------------------------------------------------
    // Reject Pickup
    // ------------------------------------------------------------------
    @PostMapping("/reject/{token}")
    public ResponseEntity<Map<String, Object>> rejectPickup(
            @PathVariable String token,
            @RequestBody DriverPickupRejectDto dto) {

        log.info("Reject pickup: token={}", token);
        try {
            driverPickupService.rejectPickup(token, dto);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }
}
