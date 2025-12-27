package com.elocate.elocate.service;

import com.elocate.elocate.model.RecycleRequest;
import com.elocate.elocate.model.UserWallet;
import com.elocate.elocate.model.WalletTransaction;
import com.elocate.elocate.repository.UserWalletRepository;
import com.elocate.elocate.repository.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Service for managing user wallet and transactions
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class WalletService {
    
    private final UserWalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;
    
    /**
     * Credit wallet with points and create transaction
     */
    @Transactional
    public void creditWallet(UUID userId, RecycleRequest recycleRequestId, BigDecimal points, String description) {
        log.info("Crediting wallet for user: {} with {} points", userId, points);
        
        // Get or create wallet
        UserWallet wallet = walletRepository.findByUserId(userId)
                .orElseGet(() -> {
                    log.info("Creating new wallet for user: {}", userId);
                    UserWallet newWallet = UserWallet.builder()
                            .userId(userId)
                            .pointsBalance(BigDecimal.ZERO)
                            .build();
                    return walletRepository.save(newWallet);
                });
        
        // Update balance
        BigDecimal newBalance = wallet.getPointsBalance().add(points);
        wallet.setPointsBalance(newBalance);
        walletRepository.save(wallet);
        
 log.info("Wallet balance updated: {} → {}", wallet.getPointsBalance().subtract(points), newBalance);
        
        // Create transaction record
        WalletTransaction transaction = WalletTransaction.builder()
                .userId(userId)
                .recycleRequest(recycleRequestId)
                .transactionType("CREDIT")
                .points(points)
                .description(description)
                .build();
        
        transactionRepository.save(transaction);
        log.info("Wallet transaction created for recycle request: {}", recycleRequestId);
    }
    
    /**
     * Get wallet balance
     */
    @Transactional(readOnly = true)
    public BigDecimal getWalletBalance(UUID userId) {
        log.info("Fetching wallet balance for user: {}", userId);
        return walletRepository.findByUserId(userId)
                .map(UserWallet::getPointsBalance)
                .orElse(BigDecimal.ZERO);
    }
}
