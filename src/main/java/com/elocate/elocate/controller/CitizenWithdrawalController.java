package com.elocate.elocate.controller;

import com.elocate.elocate.context.UserContextHolder;
import com.elocate.elocate.dto.WithdrawalRequestDto;
import com.elocate.elocate.dto.WithdrawalRequestResponse;
import com.elocate.elocate.service.WithdrawalService;
import jakarta.validation.Valid;
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
@RequestMapping("/api/v1/citizen/withdrawal")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CITIZEN')")
public class CitizenWithdrawalController {

    private final WithdrawalService withdrawalService;

    /** Submit a withdrawal request */
    @PostMapping
    public ResponseEntity<WithdrawalRequestResponse> requestWithdrawal(
            @Valid @RequestBody WithdrawalRequestDto dto) {
        UUID userId = UserContextHolder.getContext().getUserId();
        log.info("Citizen {} requesting withdrawal of ₹{}", userId, dto.getAmount());
        return ResponseEntity.ok(withdrawalService.requestWithdrawal(userId, dto));
    }

    /** List own withdrawal requests */
    @GetMapping
    public ResponseEntity<List<WithdrawalRequestResponse>> getMyWithdrawals() {
        UUID userId = UserContextHolder.getContext().getUserId();
        return ResponseEntity.ok(withdrawalService.getByUser(userId));
    }
}
