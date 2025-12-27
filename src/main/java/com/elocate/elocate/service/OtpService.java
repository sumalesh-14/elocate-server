package com.elocate.elocate.service;

import com.elocate.elocate.model.Otp;
import com.elocate.elocate.model.OtpType;
import com.elocate.elocate.repository.OtpRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

/**
 * Service for OTP generation, validation, and management
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OtpService {
    
    private final OtpRepository otpRepository;
    private final EmailService emailService;
    
    @Value("${app.otp.expiry-minutes:10}")
    private int otpExpiryMinutes;
    
    private static final SecureRandom random = new SecureRandom();
    
    /**
     * Generate and send OTP to email
     * 
     * @param email Email address
     * @param otpType Type of OTP
     * @return Generated OTP code (for testing purposes, remove in production)
     */
    @Transactional
    public String generateAndSendOtp(String email, OtpType otpType) {
        log.info("Generating OTP for email: {}, type: {}", email, otpType);
        
        // Invalidate all previous OTPs for this email and type
        otpRepository.invalidateOldOtps(email, otpType);
        
        // Generate 6-digit OTP
        String otpCode = generateOtpCode();
        
        // Calculate expiry time
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(otpExpiryMinutes);
        
        // Save OTP to database
        Otp otp = Otp.builder()
                .email(email)
                .otpCode(otpCode)
                .otpType(otpType)
                .expiresAt(expiresAt)
                .isUsed(false)
                .build();
        
        otpRepository.save(otp);
        log.info("OTP saved for email: {}, expires at: {}", email, expiresAt);
        
        // Send OTP via email
        emailService.sendOtpEmail(email, otpCode, otpType);
        
        return otpCode; // Return for testing, should be removed in production
    }
    
    /**
     * Verify OTP code
     * 
     * @param email Email address
     * @param otpCode OTP code to verify
     * @param otpType Type of OTP
     * @return true if valid, false otherwise
     */
    @Transactional
    public boolean verifyOtp(String email, String otpCode, OtpType otpType) {
        log.info("Verifying OTP for email: {}, type: {}", email, otpType);
        
        Otp otp = otpRepository.findValidOtp(email, otpType, LocalDateTime.now())
                .orElse(null);
        
        if (otp == null) {
            log.warn("No valid OTP found for email: {}", email);
            return false;
        }
        
        if (!otp.getOtpCode().equals(otpCode)) {
            log.warn("Invalid OTP code for email: {}", email);
            return false;
        }
        
        // Mark OTP as used
        otp.setIsUsed(true);
        otpRepository.save(otp);
        
        log.info("OTP verified successfully for email: {}", email);
        return true;
    }
    
    /**
     * Check if valid OTP exists for email and type
     * 
     * @param email Email address
     * @param otpType OTP type
     * @return true if valid OTP exists
     */
    public boolean hasValidOtp(String email, OtpType otpType) {
        return otpRepository.findValidOtp(email, otpType, LocalDateTime.now())
                .isPresent();
    }
    
    /**
     * Generate 6-digit OTP code
     * TODO: Currently returns static "123456" for testing - replace with random generation in production
     */
    private String generateOtpCode() {
                int otp = 100000 + random.nextInt(900000); // 6-digit number
        //return String.valueOf(otp);
        // Static OTP for testing purposes
        return "123456";
    }
}
