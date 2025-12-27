package com.elocate.elocate.controller;

import com.elocate.elocate.dto.WalletBalanceResponse;
import com.elocate.elocate.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/wallet")
@RequiredArgsConstructor
public class WalletController {
    
    private final WalletService walletService;
    
    /**
     * Get wallet balance for a user
     */
    @GetMapping("/balance")
    public ResponseEntity<WalletBalanceResponse> getWalletBalance(@RequestParam UUID userId) {
        log.info("GET /api/v1/wallet/balance - userId: {}", userId);
        
        BigDecimal balance = walletService.getWalletBalance(userId);
        
        WalletBalanceResponse response = WalletBalanceResponse.builder()
                .pointsBalance(balance)
                .lifetimeEarned(balance) // For now, same as balance
                .totalTransactions(0) // Could be enhanced to count transactions
                .build();
        
        return ResponseEntity.ok(response);
    }
}
