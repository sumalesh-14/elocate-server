package com.elocate.elocate.controller;

import com.elocate.elocate.dto.CitizenDashboardResponse;
import com.elocate.elocate.dto.RecycleRequestResponse;
import com.elocate.elocate.dto.WalletBalanceResponse;
import com.elocate.elocate.model.RecycleStatus;
import com.elocate.elocate.model.WalletTransaction;
import com.elocate.elocate.service.RecycleRequestService;
import com.elocate.elocate.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Citizen dashboard controller
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/citizen/dashboard")
@RequiredArgsConstructor
public class CitizenDashboardController {

    private final RecycleRequestService recycleRequestService;
    private final WalletService walletService;

    /**
     * Get citizen dashboard data
     */
    @GetMapping
    public ResponseEntity<CitizenDashboardResponse> getDashboard(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        UUID userId = UUID.fromString(userDetails.getUsername());
        log.info("Fetching dashboard for citizen: {}", userId);
        
        // Get all requests
        List<RecycleRequestResponse> allRequests = recycleRequestService.getRecycleRequests(
                userId, null, null);
        
        // Calculate stats
        long totalRequests = allRequests.size();
        long completedRequests = allRequests.stream()
                .filter(r -> RecycleStatus.RECYCLED.name().equals(r.getStatus()))
                .count();
        
        // Get wallet balance
        WalletBalanceResponse walletBalance = walletService.getWalletBalanceWithConversion(userId);
        
        // Get recent requests (last 5)
        List<RecycleRequestResponse> recentRequests = allRequests.stream()
                .limit(5)
                .collect(Collectors.toList());
        
        // Get recent transactions (last 5)
        Page<WalletTransaction> transactionsPage = walletService.getTransactions(
                userId, PageRequest.of(0, 5));
        
        // Calculate environmental impact (simplified)
        double co2Saved = completedRequests * 2.5; // 2.5 kg CO2 per device (example)
        
        CitizenDashboardResponse response = CitizenDashboardResponse.builder()
                .totalRequests(totalRequests)
                .completedRequests(completedRequests)
                .walletBalance(walletBalance.getMonetaryAmount())
                .currencyCode(walletBalance.getCurrencyCode())
                .recentRequests(recentRequests)
                .recentTransactions(transactionsPage.getContent())
                .environmentalImpact(CitizenDashboardResponse.EnvironmentalImpact.builder()
                        .devicesRecycled(completedRequests)
                        .co2SavedKg(co2Saved)
                        .build())
                .build();
        
        return ResponseEntity.ok(response);
    }
}
