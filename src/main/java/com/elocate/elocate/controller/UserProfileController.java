package com.elocate.elocate.controller;

import com.elocate.elocate.context.UserContextHolder;
import com.elocate.elocate.dto.*;
import com.elocate.elocate.service.AuthenticationService;
import com.elocate.elocate.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService profileService;
    private final AuthenticationService authenticationService;

    /** GET /api/v1/profile */
    @GetMapping
    public ResponseEntity<UserProfileResponse> getProfile() {
        UUID userId = UserContextHolder.getContext().getUserId();
        return ResponseEntity.ok(profileService.getUserProfile(userId));
    }

    /** PUT /api/v1/profile — update name + address */
    @PutMapping
    public ResponseEntity<UserProfileResponse> updateProfile(@Valid @RequestBody UpdateProfileDto dto) {
        UUID userId = UserContextHolder.getContext().getUserId();
        return ResponseEntity.ok(profileService.updateProfile(userId, dto));
    }

    /** PUT /api/v1/profile/facility — update facility name, hours, address */
    @PutMapping("/facility")
    public ResponseEntity<Map<String, String>> updateFacilityProfile(
            @Valid @RequestBody UpdateFacilityProfileDto dto) {
        UUID userId = UserContextHolder.getContext().getUserId();
        profileService.updateFacilityProfile(userId, dto);
        return ResponseEntity.ok(Map.of("message", "Facility profile updated successfully"));
    }

    /** POST /api/v1/profile/change-password */
    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(@Valid @RequestBody ChangePasswordDto dto) {
        UUID userId = UserContextHolder.getContext().getUserId();
        String msg = profileService.changePassword(userId, dto);
        return ResponseEntity.ok(Map.of("message", msg));
    }

    /** POST /api/v1/profile/delete-account */
    @PostMapping("/delete-account")
    public ResponseEntity<Map<String, String>> deleteAccount() {
        UUID userId = UserContextHolder.getContext().getUserId();
        String msg = profileService.deleteAccount(userId);
        return ResponseEntity.ok(Map.of("message", msg));
    }

    /** GET /api/v1/profile/notifications */
    @GetMapping("/notifications")
    public ResponseEntity<NotificationPreferencesDto> getNotifications() {
        UUID userId = UserContextHolder.getContext().getUserId();
        return ResponseEntity.ok(profileService.getNotificationPreferences(userId));
    }

    /** PUT /api/v1/profile/notifications */
    @PutMapping("/notifications")
    public ResponseEntity<NotificationPreferencesDto> updateNotifications(
            @RequestBody NotificationPreferencesDto dto) {
        UUID userId = UserContextHolder.getContext().getUserId();
        return ResponseEntity.ok(profileService.updateNotificationPreferences(userId, dto));
    }

    /** POST /api/v1/profile/change-email-request */
    @PostMapping("/change-email-request")
    public ResponseEntity<Map<String, String>> requestEmailChange(@RequestParam String newEmail) {
        UUID userId = UserContextHolder.getContext().getUserId();
        String message = authenticationService.requestEmailChange(userId, newEmail);
        return ResponseEntity.ok(Map.of("message", message));
    }

    /** POST /api/v1/profile/verify-email-change */
    @PostMapping("/verify-email-change")
    public ResponseEntity<Map<String, String>> verifyEmailChange(
            @RequestParam String newEmail, @RequestParam String otp) {
        UUID userId = UserContextHolder.getContext().getUserId();
        String message = authenticationService.verifyEmailChange(userId, newEmail, otp);
        return ResponseEntity.ok(Map.of("message", message));
    }

    /** POST /api/v1/profile/change-mobile-request */
    @PostMapping("/change-mobile-request")
    public ResponseEntity<Map<String, String>> requestMobileChange(@RequestParam String newMobile) {
        UUID userId = UserContextHolder.getContext().getUserId();
        String message = profileService.requestMobileChange(userId, newMobile);
        return ResponseEntity.ok(Map.of("message", message));
    }

    /** POST /api/v1/profile/verify-mobile-change */
    @PostMapping("/verify-mobile-change")
    public ResponseEntity<Map<String, String>> verifyMobileChange(
            @RequestParam String newMobile, @RequestParam String otp) {
        UUID userId = UserContextHolder.getContext().getUserId();
        String message = profileService.verifyMobileChange(userId, newMobile, otp);
        return ResponseEntity.ok(Map.of("message", message));
    }
}
