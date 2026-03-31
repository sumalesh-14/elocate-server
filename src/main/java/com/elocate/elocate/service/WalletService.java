package com.elocate.elocate.service;

import com.elocate.elocate.dto.WalletBalanceResponse;
import com.elocate.elocate.dto.WalletStatsResponse;
import com.elocate.elocate.dto.WalletTransactionResponse;
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

                // Create transaction record — points are already in INR, no conversion needed
                WalletTransaction transaction = WalletTransaction.builder()
                                .userId(userId)
                                .recycleRequest(recycleRequestId)
                                .points(points)
                                .transactionType("CREDIT")
                                .description(description)
                                .conversionRate(BigDecimal.ONE)
                                .monetaryAmount(points.setScale(2, RoundingMode.HALF_UP))
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

                // Create adjustment transaction — amount is already in INR
                String transactionType = pointsDifference.compareTo(BigDecimal.ZERO) > 0 ? "CREDIT" : "DEBIT";
                String description = String.format("Price adjustment: %s. Reason: %s",
                                transactionType.toLowerCase(), reason);

                WalletTransaction transaction = WalletTransaction.builder()
                                .userId(userId)
                                .recycleRequest(recycleRequest)
                                .points(pointsDifference.abs())
                                .transactionType(transactionType)
                                .description(description)
                                .conversionRate(BigDecimal.ONE)
                                .monetaryAmount(pointsDifference.abs().setScale(2, RoundingMode.HALF_UP))
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

                // points_balance IS the INR amount — no conversion needed
                return WalletBalanceResponse.builder()
                                .pointsBalance(wallet.getPointsBalance())
                                .monetaryAmount(wallet.getPointsBalance().setScale(2, RoundingMode.HALF_UP))
                                .currencyCode("INR")
                                .conversionRate(BigDecimal.ONE)
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

        /**
         * Get full wallet stats: balance, highest tx, rank, all transactions
         */
        @Transactional(readOnly = true)
        public WalletStatsResponse getWalletStats(UUID userId) {
                log.info("Fetching wallet stats for user: {}", userId);

                UserWallet wallet = walletRepository.findByUserId(userId)
                                .orElseGet(() -> UserWallet.builder()
                                                .userId(userId)
                                                .pointsBalance(BigDecimal.ZERO)
                                                .currencyCode(defaultCurrencyCode)
                                                .pointsToMoneyRate(defaultPointsToMoneyRate)
                                                .build());

                List<WalletTransaction> txList = transactionRepository.findByUserIdOrderByCreatedAtDesc(userId);

                // points_balance is already INR — no conversion
                BigDecimal totalPoints = wallet.getPointsBalance();
                BigDecimal highest = transactionRepository.findHighestTransactionByUserId(userId)
                                .orElse(BigDecimal.ZERO);

                int rank = transactionRepository.findUserRank(userId).orElse(0);
                long totalUsers = transactionRepository.countDistinctUsers();

                String tier = computeTier(rank, totalUsers);

                List<WalletTransactionResponse> txResponses = txList.stream().map(t -> {
                                String requestNumber = t.getRecycleRequest() != null
                                                ? t.getRecycleRequest().getRequestNumber()
                                                : null;
                                String desc = requestNumber != null
                                                ? "Recycling reward · " + requestNumber
                                                : t.getDescription();
                                return WalletTransactionResponse.builder()
                                                .id(t.getId())
                                                .transactionType(t.getTransactionType())
                                                .amount(t.getPoints())
                                                .description(desc)
                                                .recycleRequestNumber(requestNumber)
                                                .createdAt(t.getCreatedAt())
                                                .build();
                }).toList();

                return WalletStatsResponse.builder()
                                .totalAmount(totalPoints.setScale(2, RoundingMode.HALF_UP))
                                .highestSingleAmount(highest)
                                .totalTransactions(txList.size())
                                .userRank(rank)
                                .totalUsersRanked(totalUsers)
                                .rankTier(tier)
                                .transactions(txResponses)
                                .build();
        }

        private String computeTier(int rank, long total) {
                if (rank <= 0 || total == 0) return "UNRANKED";
                double pct = (double) rank / total;
                if (pct <= 0.05) return "PLATINUM";
                if (pct <= 0.20) return "GOLD";
                if (pct <= 0.50) return "SILVER";
                return "BRONZE";
        }
}
