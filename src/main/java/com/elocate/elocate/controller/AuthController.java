package com.elocate.elocate.controller;

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
}