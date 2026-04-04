package com.elocate.elocate.controller;

import com.elocate.elocate.dto.WalletBalanceResponse;
import com.elocate.elocate.dto.WalletStatsResponse;
import com.elocate.elocate.dto.WalletTransactionResponse;
import com.elocate.elocate.model.WalletTransaction;
import com.elocate.elocate.repository.WalletTransactionRepository;
import com.elocate.elocate.repository.UserRepository;
import com.elocate.elocate.model.User;
import com.elocate.elocate.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;
    private final WalletTransactionRepository txRepo;
    private final UserRepository userRepo;

    @GetMapping("/balance")
    public ResponseEntity<WalletBalanceResponse> getWalletBalance(@RequestParam UUID userId) {
        BigDecimal balance = walletService.getWalletBalance(userId);
        return ResponseEntity.ok(WalletBalanceResponse.builder()
                .pointsBalance(balance).lifetimeEarned(balance).totalTransactions(0).build());
    }

    @GetMapping("/stats")
    public ResponseEntity<WalletStatsResponse> getWalletStats(@RequestParam UUID userId) {
        log.info("GET /api/v1/wallet/stats - userId: {}", userId);
        return ResponseEntity.ok(walletService.getWalletStats(userId));
    }

    /** GET /api/v1/wallet/transactions?userId= — all transactions for a citizen */
    @GetMapping("/transactions")
    public ResponseEntity<List<WalletTransactionResponse>> getTransactionsByUser(@RequestParam UUID userId) {
        log.info("GET /api/v1/wallet/transactions - userId: {}", userId);
        String userName = userRepo.findById(userId).map(User::getFullName).orElse("Unknown");
        List<WalletTransactionResponse> result = txRepo.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(t -> toResponse(t, userName)).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    /** GET /api/v1/wallet/transactions/facility?facilityId= — RECYCLED transactions for a facility */
    @GetMapping("/transactions/facility")
    public ResponseEntity<List<WalletTransactionResponse>> getTransactionsByFacility(@RequestParam UUID facilityId) {
        log.info("GET /api/v1/wallet/transactions/facility - facilityId: {}", facilityId);
        List<WalletTransactionResponse> result = txRepo.findByFacilityId(facilityId)
                .stream().map(t -> {
                    String name = userRepo.findById(t.getUserId()).map(User::getFullName).orElse("Unknown");
                    return toResponse(t, name);
                }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    /** GET /api/v1/wallet/transactions/facility/withdrawals?facilityId= — WITHDRAWN/REFUNDED for facility's citizens */
    @GetMapping("/transactions/facility/withdrawals")
    public ResponseEntity<List<WalletTransactionResponse>> getWithdrawalsByFacility(@RequestParam UUID facilityId) {
        log.info("GET /api/v1/wallet/transactions/facility/withdrawals - facilityId: {}", facilityId);
        List<WalletTransactionResponse> result = txRepo.findWithdrawalsByFacilityCitizens(facilityId)
                .stream().map(t -> {
                    String name = userRepo.findById(t.getUserId()).map(User::getFullName).orElse("Unknown");
                    return toResponse(t, name);
                }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    private WalletTransactionResponse toResponse(WalletTransaction t, String citizenName) {
        return WalletTransactionResponse.builder()
                .id(t.getId())
                .transactionType(t.getTransactionType())
                .amount(t.getPoints())
                .description(t.getDescription())
                .citizenName(citizenName)
                .recycleRequestNumber(t.getRecycleRequest() != null ? t.getRecycleRequest().getRequestNumber() : null)
                .createdAt(t.getCreatedAt())
                .build();
    }
}
