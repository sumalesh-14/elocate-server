package com.elocate.elocate.service;

import com.elocate.elocate.dto.WithdrawalRequestDto;
import com.elocate.elocate.dto.WithdrawalRequestResponse;
import com.elocate.elocate.model.*;
import com.elocate.elocate.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class WithdrawalService {

    private final WithdrawalRequestRepository withdrawalRepo;
    private final UserWalletRepository walletRepo;
    private final WalletTransactionRepository txRepo;
    private final UserRepository userRepo;
    private final EmailService emailService;

    private static final BigDecimal MIN_WITHDRAWAL = BigDecimal.valueOf(100);

    @Transactional
    public WithdrawalRequestResponse requestWithdrawal(UUID userId, WithdrawalRequestDto dto) {
        // Validate minimum
        if (dto.getAmount().compareTo(MIN_WITHDRAWAL) < 0) {
            throw new IllegalArgumentException("Minimum withdrawal amount is ₹100");
        }

        // No duplicate pending
        if (withdrawalRepo.existsByUserIdAndStatus(userId, WithdrawalStatus.PENDING)) {
            throw new IllegalStateException("You already have a pending withdrawal request");
        }

        // Check balance
        UserWallet wallet = walletRepo.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Wallet not found"));
        if (wallet.getPointsBalance().compareTo(dto.getAmount()) < 0) {
            throw new IllegalArgumentException("Insufficient wallet balance");
        }

        // Deduct from wallet (hold) — no transaction record, just balance hold
        wallet.setPointsBalance(wallet.getPointsBalance().subtract(dto.getAmount()));
        walletRepo.save(wallet);

        // Create withdrawal record
        WithdrawalRequest wr = WithdrawalRequest.builder()
                .userId(userId)
                .amount(dto.getAmount())
                .accountHolderName(dto.getAccountHolderName())
                .mobileNumber(dto.getMobileNumber())
                .accountNumber(dto.getAccountNumber())
                .bankName(dto.getBankName())
                .ifscCode(dto.getIfscCode())
                .upiId(dto.getUpiId())
                .email(dto.getEmail())
                .status(WithdrawalStatus.PENDING)
                .build();
        withdrawalRepo.save(wr);

        log.info("Withdrawal request created for user {} amount ₹{}", userId, dto.getAmount());
        return toResponse(wr, null);
    }

    @Transactional
    public WithdrawalRequestResponse approveWithdrawal(UUID requestId, UUID processedBy, String note) {
        WithdrawalRequest wr = withdrawalRepo.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Withdrawal request not found"));
        if (wr.getStatus() != WithdrawalStatus.PENDING) {
            throw new IllegalStateException("Request is not in PENDING state");
        }

        wr.setStatus(WithdrawalStatus.APPROVED);
        wr.setProcessedBy(processedBy);
        wr.setAdminNote(note);
        wr.setProcessedAt(LocalDateTime.now());
        withdrawalRepo.save(wr);

        // WITHDRAWN transaction — deduction is now officially recorded
        txRepo.save(WalletTransaction.builder()
                .userId(wr.getUserId())
                .points(wr.getAmount())
                .transactionType("WITHDRAWN")
                .description("Withdrawal approved — ₹" + wr.getAmount() + " to " + wr.getBankName() + " (" + masked(wr.getAccountNumber()) + ")")
                .conversionRate(BigDecimal.ONE)
                .monetaryAmount(wr.getAmount().setScale(2, java.math.RoundingMode.HALF_UP))
                .build());

        // Notify citizen
        userRepo.findById(wr.getUserId()).ifPresent(user -> {
            if (user.getEmail() != null) {
                emailService.sendWithdrawalApprovedEmail(
                        user.getEmail(), user.getFullName(), wr.getAmount(),
                        wr.getBankName(), masked(wr.getAccountNumber()));
            }
        });

        log.info("Withdrawal {} approved by {}", requestId, processedBy);
        return toResponse(wr, null);
    }

    @Transactional
    public WithdrawalRequestResponse rejectWithdrawal(UUID requestId, UUID processedBy, String note) {
        WithdrawalRequest wr = withdrawalRepo.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Withdrawal request not found"));
        if (wr.getStatus() != WithdrawalStatus.PENDING) {
            throw new IllegalStateException("Request is not in PENDING state");
        }

        wr.setStatus(WithdrawalStatus.REJECTED);
        wr.setProcessedBy(processedBy);
        wr.setAdminNote(note);
        wr.setProcessedAt(LocalDateTime.now());
        withdrawalRepo.save(wr);

        // Refund wallet
        UserWallet wallet = walletRepo.findByUserId(wr.getUserId())
                .orElseThrow(() -> new IllegalStateException("Wallet not found"));
        wallet.setPointsBalance(wallet.getPointsBalance().add(wr.getAmount()));
        walletRepo.save(wallet);

        // REFUNDED transaction — balance restored
        txRepo.save(WalletTransaction.builder()
                .userId(wr.getUserId())
                .points(wr.getAmount())
                .transactionType("REFUNDED")
                .description("Withdrawal rejected — amount refunded. Reason: " + (note != null ? note : "N/A"))
                .conversionRate(BigDecimal.ONE)
                .monetaryAmount(wr.getAmount().setScale(2, java.math.RoundingMode.HALF_UP))
                .build());

        // Notify citizen
        userRepo.findById(wr.getUserId()).ifPresent(user -> {
            if (user.getEmail() != null) {
                emailService.sendWithdrawalRejectedEmail(
                        user.getEmail(), user.getFullName(), wr.getAmount(), note);
            }
        });

        log.info("Withdrawal {} rejected by {}", requestId, processedBy);
        return toResponse(wr, null);
    }

    @Transactional(readOnly = true)
    public List<WithdrawalRequestResponse> getByUser(UUID userId) {
        return withdrawalRepo.findByUserIdOrderByRequestedAtDesc(userId)
                .stream().map(w -> toResponse(w, null)).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<WithdrawalRequestResponse> getPending() {
        return withdrawalRepo.findByStatusOrderByRequestedAtAsc(WithdrawalStatus.PENDING)
                .stream().map(w -> {
                    String name = userRepo.findById(w.getUserId())
                            .map(User::getFullName).orElse("Unknown");
                    String email = userRepo.findById(w.getUserId())
                            .map(User::getEmail).orElse(null);
                    return toResponse(w, name, email);
                }).collect(Collectors.toList());
    }

    private WithdrawalRequestResponse toResponse(WithdrawalRequest w, String citizenName) {
        return toResponse(w, citizenName, null);
    }

    private WithdrawalRequestResponse toResponse(WithdrawalRequest w, String citizenName, String citizenEmail) {
        return WithdrawalRequestResponse.builder()
                .id(w.getId())
                .amount(w.getAmount())
                .accountHolderName(w.getAccountHolderName())
                .maskedAccountNumber(masked(w.getAccountNumber()))
                .fullAccountNumber(w.getAccountNumber())
                .bankName(w.getBankName())
                .ifscCode(w.getIfscCode())
                .mobileNumber(w.getMobileNumber())
                .upiId(w.getUpiId())
                .email(w.getEmail())
                .status(w.getStatus())
                .adminNote(w.getAdminNote())
                .requestedAt(w.getRequestedAt())
                .processedAt(w.getProcessedAt())
                .citizenName(citizenName)
                .citizenEmail(citizenEmail)
                .build();
    }

    private String masked(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 4) return accountNumber;
        return "****" + accountNumber.substring(accountNumber.length() - 4);
    }
}
