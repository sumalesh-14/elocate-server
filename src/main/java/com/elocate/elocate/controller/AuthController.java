package com.elocate.elocate.controller;

import com.elocate.elocate.dto.RequestLoginOtpDto;
import com.elocate.elocate.dto.VerifyLoginOtpDto;
import com.elocate.elocate.context.UserContext;
import com.elocate.elocate.context.UserContextHolder;
import com.elocate.elocate.dto.*;
import com.elocate.elocate.service.AuthenticationService;
import com.elocate.elocate.service.OtpService;
import com.elocate.elocate.service.UserRegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller for authentication operations
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AuthController {

    private final UserRegistrationService userRegistrationService;
    private final AuthenticationService authenticationService;
    private final OtpService otpService;

    /**
     * Register new user
     * Sends OTP to email for verification
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody UserRegistrationDto dto) {
        System.out.println("=== USER REGISTRATION BACKEND ===");
        System.out.println("Endpoint: POST /api/v1/auth/register");
        System.out.println("Request: " + dto);
        System.out.println("Email: " + dto.getEmail());
        System.out.println("Full Name: " + dto.getFullName());
        System.out.println("Role: " + dto.getRole());
        System.out.println("================================");

        log.info("POST /api/v1/auth/register - email: {}", dto.getEmail());

        String message = userRegistrationService.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", message));
    }

    /**
     * Verify email with OTP
     */
    @PostMapping("/verify-email")
    public ResponseEntity<Map<String, String>> verifyEmail(@Valid @RequestBody VerifyEmailDto dto) {
        log.info("POST /api/v1/auth/verify-email - email: {}", dto.getEmail());

        String message = authenticationService.verifyEmail(dto.getEmail(), dto.getOtp());
        return ResponseEntity.ok(Map.of("message", message));
    }

    /**
     * Resend OTP
     */
    @PostMapping("/resend-otp")
    public ResponseEntity<Map<String, String>> resendOtp(@Valid @RequestBody ResendOtpDto dto) {
        log.info("POST /api/v1/auth/resend-otp - email: {}, type: {}", dto.getEmail(), dto.getType());

        otpService.generateAndSendOtp(dto.getEmail(), dto.getType());
        return ResponseEntity.ok(Map.of("message", "OTP sent successfully"));
    }

    /**
     * Login with email and password
     * Firebase verifies password via REST API
     * Returns user profile with Firebase ID token
     */
    @PostMapping("/login")
    public ResponseEntity<UserProfileResponse> login(@Valid @RequestBody LoginRequestDto dto) {
        log.info("POST /api/v1/auth/login - email: {}", dto.getEmail());

        UserProfileResponse response = authenticationService.login(dto);
        return ResponseEntity.ok(response);
    }

    /**
     * Request password reset via Firebase
     * Firebase will send password reset email directly
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestParam String email) {
        log.info("POST /api/v1/auth/forgot-password - email: {}", email);

        String message = authenticationService.forgotPassword(email);
        return ResponseEntity.ok(Map.of("message", message));
    }

    /**
     * Refresh access tokens
     */
    @PostMapping("/refresh")
    public ResponseEntity<UserProfileResponse> refresh(@Valid @RequestBody TokenRefreshRequest request) {
        log.info("POST /api/v1/auth/refresh");
        UserProfileResponse response = authenticationService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    /**
     * Request OTP for passwordless email login
     */
    @PostMapping("/request-login-otp")
    public ResponseEntity<Map<String, String>> requestLoginOtp(@Valid @RequestBody RequestLoginOtpDto dto) {
        log.info("POST /api/v1/auth/request-login-otp - email: {}", dto.getEmail());
        String message = authenticationService.requestLoginOtp(dto.getEmail());
        return ResponseEntity.ok(Map.of("message", message));
    }

    /**
     * Verify OTP and complete passwordless login — returns JWT token
     */
    @PostMapping("/verify-login-otp")
    public ResponseEntity<UserProfileResponse> verifyLoginOtp(@Valid @RequestBody VerifyLoginOtpDto dto) {
        log.info("POST /api/v1/auth/verify-login-otp - email: {}", dto.getEmail());
        UserProfileResponse response = authenticationService.verifyLoginOtp(dto.getEmail(), dto.getOtp());
        return ResponseEntity.ok(response);
    }

    /**
     * Validate the current session token
     * Returns 200 OK if token is valid, otherwise 401/403 handled by filter
     */
    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validate() {
        UserContext context = UserContextHolder.getContext();
        log.info("GET /api/v1/auth/validate - userId: {}", context.getUserId());

        return ResponseEntity.ok(Map.of(
                "valid", true,
                "userId", context.getUserId(),
                "email", context.getEmail(),
                "fullName", context.getFullName()));
    }
}