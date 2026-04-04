package com.elocate.elocate.controller;

import com.elocate.elocate.context.UserContextHolder;
import com.elocate.elocate.dto.WithdrawalRequestResponse;
import com.elocate.elocate.service.WithdrawalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/intermediary/withdrawals")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('INTERMEDIARY', 'PARTNER')")
public class IntermediaryWithdrawalController {

    private final WithdrawalService withdrawalService;

    /** List all PENDING withdrawal requests */
    @GetMapping("/pending")
    public ResponseEntity<List<WithdrawalRequestResponse>> getPending() {
        return ResponseEntity.ok(withdrawalService.getPending());
    }

    /** Approve a withdrawal */
    @PostMapping("/{id}/approve")
    public ResponseEntity<Map<String, String>> approve(
            @PathVariable UUID id,
            @RequestBody(required = false) Map<String, String> body) {
        UUID processedBy = UserContextHolder.getContext().getUserId();
        String note = body != null ? body.get("note") : null;
        withdrawalService.approveWithdrawal(id, processedBy, note);
        return ResponseEntity.ok(Map.of("message", "Withdrawal approved successfully"));
    }

    /** Reject a withdrawal */
    @PostMapping("/{id}/reject")
    public ResponseEntity<Map<String, String>> reject(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        UUID processedBy = UserContextHolder.getContext().getUserId();
        String note = body.get("note");
        withdrawalService.rejectWithdrawal(id, processedBy, note);
        return ResponseEntity.ok(Map.of("message", "Withdrawal rejected and amount refunded"));
    }
}
