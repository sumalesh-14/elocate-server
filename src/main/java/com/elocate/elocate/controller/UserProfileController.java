package com.elocate.elocate.controller;

import com.elocate.elocate.dto.UpdateProfileDto;
import com.elocate.elocate.dto.UserProfileResponse;
import com.elocate.elocate.service.AuthenticationService;
import com.elocate.elocate.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * Controller for user profile management
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class UserProfileController {
    
    private final UserProfileService profileService;
    private final AuthenticationService authenticationService;
    
    /**
     * Get current user's complete profile
     * 
     * Returns:
     * - User details (name, mobile, email)
     * - Address (user's single default address)
     * - Wallet (current points balance)
     */
    @GetMapping
    public ResponseEntity<UserProfileResponse> getProfile(@RequestParam UUID userId) {
        log.info("GET /api/v1/profile - userId: {}", userId);
        
        UserProfileResponse response = profileService.getUserProfile(userId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Update user profile
     * 
     * Updates:
     * - Personal details (name, email)
     * - Address details (all address fields)
     * 
     * Note: Mobile number change requires separate OTP flow
     */
    @PutMapping
    public ResponseEntity<UserProfileResponse> updateProfile(
            @RequestParam UUID userId,
            @Valid @RequestBody UpdateProfileDto dto) {
        log.info("PUT /api/v1/profile - userId: {}", userId);
        
        UserProfileResponse response = profileService.updateProfile(userId, dto);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Request email change with OTP
     */
    @PostMapping("/change-email-request")
    public ResponseEntity<Map<String, String>> requestEmailChange(
            @RequestParam UUID userId,
            @RequestParam String newEmail) {
        log.info("POST /api/v1/profile/change-email-request - userId: {}, newEmail: {}", userId, newEmail);
        
        String message = authenticationService.requestEmailChange(userId, newEmail);
        return ResponseEntity.ok(Map.of("message", message));
    }
    
    /**
     * Verify email change with OTP
     */
    @PostMapping("/verify-email-change")
    public ResponseEntity<Map<String, String>> verifyEmailChange(
            @RequestParam UUID userId,
            @RequestParam String newEmail,
            @RequestParam String otp) {
        log.info("POST /api/v1/profile/verify-email-change - userId: {}", userId);
        
        String message = authenticationService.verifyEmailChange(userId, newEmail, otp);
        return ResponseEntity.ok(Map.of("message", message));
    }
}
