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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

/**
 * Internal endpoint called by elocate-whatsapp-bot to perform driver pickup actions.
 * Uses the SAME DriverPickupService as the email-link flow — identical business logic,
 * history recording, citizen emails, etc.
 *
 * Protected by X-Internal-Secret header — NOT exposed to the public internet.
 */
@RestController
@RequestMapping("/internal/driver")
@RequiredArgsConstructor
@Slf4j
public class DriverWhatsAppActionController {

    private final DriverPickupService driverPickupService;
    private final RecycleRequestRepository recycleRequestRepository;
    private final DriverPickupTokenRepository tokenRepository;

    @Value("${whatsapp.bot.internal-secret:elocate-internal-secret}")
    private String internalSecret;

    // ------------------------------------------------------------------
    // Resolve requestNumber → active token for a given action type
    // Bot calls this first, then calls the action endpoint with the token
    // ------------------------------------------------------------------
    @GetMapping("/token/{requestNumber}/{actionType}")
    public ResponseEntity<Map<String, Object>> getToken(
            @PathVariable String requestNumber,
            @PathVariable String actionType,
            @RequestHeader("X-Internal-Secret") String secret) {

        if (!internalSecret.equals(secret)) return forbidden();

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
    public ResponseEntity<Map<String, Object>> markInProgress(
            @PathVariable String token,
            @RequestHeader("X-Internal-Secret") String secret) {

        if (!internalSecret.equals(secret)) return forbidden();

        try {
            driverPickupService.markInProgress(token);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // ------------------------------------------------------------------
    // Accept Pickup (marks PICKUP_COMPLETED, records history, emails citizen)
    // ------------------------------------------------------------------
    @PostMapping("/accept/{token}")
    public ResponseEntity<Map<String, Object>> acceptPickup(
            @PathVariable String token,
            @RequestHeader("X-Internal-Secret") String secret,
            @RequestBody(required = false) DriverPickupActionDto dto) {

        if (!internalSecret.equals(secret)) return forbidden();

        if (dto == null) dto = new DriverPickupActionDto();
        if (dto.getComments() == null) dto.setComments("Accepted via WhatsApp");

        try {
            driverPickupService.acceptPickup(token, dto);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // ------------------------------------------------------------------
    // Reject Pickup (marks PICKUP_FAILED, records history, emails citizen)
    // ------------------------------------------------------------------
    @PostMapping("/reject/{token}")
    public ResponseEntity<Map<String, Object>> rejectPickup(
            @PathVariable String token,
            @RequestHeader("X-Internal-Secret") String secret,
            @RequestBody DriverPickupRejectDto dto) {

        if (!internalSecret.equals(secret)) return forbidden();

        try {
            driverPickupService.rejectPickup(token, dto);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    private ResponseEntity<Map<String, Object>> forbidden() {
        log.warn("Unauthorized internal driver action attempt");
        return ResponseEntity.status(403).body(Map.of("success", false, "error", "Forbidden"));
    }
}
