package com.elocate.elocate.controller;

import com.elocate.elocate.dto.RecycleRequestResponse;
import com.elocate.elocate.dto.UserProfileResponse;
import com.elocate.elocate.model.User;
import com.elocate.elocate.model.WalletTransaction;
import com.elocate.elocate.service.AdminManagementService;
import com.elocate.elocate.service.RecycleRequestService;
import com.elocate.elocate.service.UserProfileService;
import com.elocate.elocate.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Admin controller for citizen management
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/citizens")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCitizenController {

    private final AdminManagementService adminManagementService;
    private final UserProfileService userProfileService;
    private final RecycleRequestService recycleRequestService;
    private final WalletService walletService;

    /**
     * Get all citizens with filters
     */
    @GetMapping
    public ResponseEntity<Page<UserProfileResponse>> getAllCitizens(
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("Admin fetching citizens - active: {}, search: {}", isActive, search);

        Pageable pageable = PageRequest.of(page, size);
        Page<User> users = adminManagementService.getCitizens(isActive, search, pageable);

        Page<UserProfileResponse> response = users.map(u -> {
            UserProfileResponse r = new UserProfileResponse();
            UserProfileResponse.UserData ud = new UserProfileResponse.UserData(
                u.getId(), u.getFullName(), u.getEmail(), u.getMobileNumber(), u.getRole(), null
            );
            r.setUser(ud);
            r.setStatus(Boolean.TRUE.equals(u.getIsActive()) ? "ACTIVE" : "INACTIVE");
            return r;
        });

        return ResponseEntity.ok(response);
    }

    /**
     * Get citizen by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserProfileResponse> getCitizenById(@PathVariable UUID id) {
        log.info("Admin fetching citizen: {}", id);
        UserProfileResponse citizen = userProfileService.getUserProfile(id);
        return ResponseEntity.ok(citizen);
    }

    /**
     * Toggle citizen active status
     */
    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<Map<String, Object>> toggleCitizenActive(
            @PathVariable UUID id,
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Admin toggling active status for citizen: {}", id);

        UUID adminUserId = UUID.fromString(userDetails.getUsername());
        Boolean active = (Boolean) body.get("active");
        String reason = (String) body.get("reason");

        if (active == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Active status is required"));
        }

        adminManagementService.toggleCitizenActive(id, active, adminUserId, reason);

        return ResponseEntity.ok(Map.of(
                "message", "Citizen status updated successfully",
                "citizenId", id.toString(),
                "active", active));
    }

    /**
     * Get citizen's recycle requests
     */
    @GetMapping("/{id}/recycle-requests")
    public ResponseEntity<List<RecycleRequestResponse>> getCitizenRecycleRequests(
            @PathVariable UUID id) {

        log.info("Admin fetching recycle requests for citizen: {}", id);
        List<RecycleRequestResponse> requests = recycleRequestService.getRecycleRequests(
                id, null, null);

        return ResponseEntity.ok(requests);
    }

    /**
     * Get citizen's wallet transactions
     */
    @GetMapping("/{id}/wallet-transactions")
    public ResponseEntity<Page<WalletTransaction>> getCitizenWalletTransactions(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("Admin fetching wallet transactions for citizen: {}", id);

        Pageable pageable = PageRequest.of(page, size);
        Page<WalletTransaction> transactions = walletService.getTransactions(id, pageable);

        return ResponseEntity.ok(transactions);
    }
}
