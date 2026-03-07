package com.elocate.elocate.service;

import com.elocate.elocate.dto.WalletBalanceResponse;
import com.elocate.elocate.model.RecycleRequest;
import com.elocate.elocate.model.UserWallet;
import com.elocate.elocate.model.WalletTransaction;
import com.elocate.elocate.repository.UserWalletRepository;
import com.elocate.elocate.repository.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
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

        @Value("${wallet.points.to.money.rate:0.01}")
        private BigDecimal defaultPointsToMoneyRate;

        @Value("${wallet.currency.code:USD}")
        private String defaultCurrencyCode;

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
                                                        .currencyCode(defaultCurrencyCode)
                                                        .pointsToMoneyRate(defaultPointsToMoneyRate)
                                                        .build();
                                        return walletRepository.save(newWallet);
                                });

                // Update balance
                BigDecimal newBalance = wallet.getPointsBalance().add(points);
                wallet.setPointsBalance(newBalance);
                walletRepository.save(wallet);

                log.info("Wallet balance updated: {} → {}", wallet.getPointsBalance().subtract(points), newBalance);

                // Calculate monetary amount
                BigDecimal conversionRate = wallet.getPointsToMoneyRate() != null ? wallet.getPointsToMoneyRate()
                                : defaultPointsToMoneyRate;
                BigDecimal monetaryAmount = points.multiply(conversionRate)
                                .setScale(2, RoundingMode.HALF_UP);

                // Create transaction record
                WalletTransaction transaction = WalletTransaction.builder()
                                .userId(userId)
                                .recycleRequest(recycleRequestId)
                                .points(points)
                                .transactionType("CREDIT")
                                .description(description)
                                .conversionRate(conversionRate)
                                .monetaryAmount(monetaryAmount)
                                .build();

                transactionRepository.save(transaction);
                log.info("Wallet transaction created for recycle request: {}", recycleRequestId);
        }

        /**
         * Adjust wallet for price change (when final points change after recycling)
         */
        @Transactional
        public void adjustWalletForPriceChange(UUID userId, RecycleRequest recycleRequest,
                        BigDecimal oldPoints, BigDecimal newPoints, String reason) {
                log.info("Adjusting wallet for user: {}, old points: {}, new points: {}",
                                userId, oldPoints, newPoints);

                BigDecimal pointsDifference = newPoints.subtract(oldPoints);

                if (pointsDifference.compareTo(BigDecimal.ZERO) == 0) {
                        log.info("No adjustment needed, points are the same");
                        return;
                }

                // Get wallet
                UserWallet wallet = walletRepository.findByUserId(userId)
                                .orElseThrow(() -> new IllegalStateException("Wallet not found for user: " + userId));

                // Update balance
                BigDecimal newBalance = wallet.getPointsBalance().add(pointsDifference);
                wallet.setPointsBalance(newBalance);
                walletRepository.save(wallet);

                log.info("Wallet balance adjusted: {} → {}", wallet.getPointsBalance().subtract(pointsDifference),
                                newBalance);

                // Calculate monetary amount
                BigDecimal conversionRate = wallet.getPointsToMoneyRate() != null ? wallet.getPointsToMoneyRate()
                                : defaultPointsToMoneyRate;
                BigDecimal monetaryAmount = pointsDifference.multiply(conversionRate)
                                .setScale(2, RoundingMode.HALF_UP);

                // Create adjustment transaction
                String transactionType = pointsDifference.compareTo(BigDecimal.ZERO) > 0 ? "CREDIT" : "DEBIT";
                String description = String.format("Price adjustment: %s. Reason: %s",
                                transactionType.toLowerCase(), reason);

                WalletTransaction transaction = WalletTransaction.builder()
                                .userId(userId)
                                .recycleRequest(recycleRequest)
                                .points(pointsDifference.abs())
                                .transactionType(transactionType)
                                .description(description)
                                .conversionRate(conversionRate)
                                .monetaryAmount(monetaryAmount.abs())
                                .build();

                transactionRepository.save(transaction);
                log.info("Wallet adjustment transaction created");
        }

        /**
         * Get wallet balance with monetary conversion
         */
        @Transactional(readOnly = true)
        public WalletBalanceResponse getWalletBalanceWithConversion(UUID userId) {
                log.info("Fetching wallet balance for user: {}", userId);

                UserWallet wallet = walletRepository.findByUserId(userId)
                                .orElseGet(() -> UserWallet.builder()
                                                .userId(userId)
                                                .pointsBalance(BigDecimal.ZERO)
                                                .currencyCode(defaultCurrencyCode)
                                                .pointsToMoneyRate(defaultPointsToMoneyRate)
                                                .build());

                BigDecimal conversionRate = wallet.getPointsToMoneyRate() != null ? wallet.getPointsToMoneyRate()
                                : defaultPointsToMoneyRate;

                BigDecimal monetaryAmount = wallet.getPointsBalance()
                                .multiply(conversionRate)
                                .setScale(2, RoundingMode.HALF_UP);

                return WalletBalanceResponse.builder()
                                .pointsBalance(wallet.getPointsBalance())
                                .monetaryAmount(monetaryAmount)
                                .currencyCode(wallet.getCurrencyCode() != null ? wallet.getCurrencyCode()
                                                : defaultCurrencyCode)
                                .conversionRate(conversionRate)
                                .build();
        }

        /**
         * Get wallet balance (points only)
         */
        @Transactional(readOnly = true)
        public BigDecimal getWalletBalance(UUID userId) {
                log.info("Fetching wallet balance for user: {}", userId);
                return walletRepository.findByUserId(userId)
                                .map(UserWallet::getPointsBalance)
                                .orElse(BigDecimal.ZERO);
        }

        /**
         * Get wallet transactions with pagination
         */
        @Transactional(readOnly = true)
        public Page<WalletTransaction> getTransactions(UUID userId, Pageable pageable) {
                log.info("Fetching wallet transactions for user: {}", userId);
                return transactionRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        }

        /**
         * Get wallet transactions by date range
         */
        @Transactional(readOnly = true)
        public List<WalletTransaction> getTransactionsByDateRange(UUID userId, LocalDate startDate, LocalDate endDate) {
                log.info("Fetching wallet transactions for user: {} from {} to {}", userId, startDate, endDate);
                return transactionRepository.findByUserIdAndCreatedAtBetween(
                                userId,
                                startDate.atStartOfDay(),
                                endDate.plusDays(1).atStartOfDay());
        }
}
