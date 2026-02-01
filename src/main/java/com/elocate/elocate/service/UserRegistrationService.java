package com.elocate.elocate.service;

import com.elocate.elocate.dto.UserRegistrationDto;
import com.elocate.elocate.model.enums.OtpType;
import com.elocate.elocate.model.User;
import com.elocate.elocate.model.UserAddress;
import com.elocate.elocate.model.UserWallet;
import com.elocate.elocate.repository.UserAddressRepository;
import com.elocate.elocate.repository.UserRepository;
import com.elocate.elocate.repository.UserWalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Service for user registration with Auth0 and email verification
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserRegistrationService {
    
    private final UserRepository userRepository;
    private final UserAddressRepository userAddressRepository;
    private final UserWalletRepository userWalletRepository;
    private final OtpService otpService;
    private final Auth0Service auth0Service;
    
    /**
     * Register new user with Auth0 and email verification
     * 
     * Flow:
     * 1. Check email and mobile uniqueness in database
     * 2. Create user in Auth0
     * 3. Create user in our database (email_verified = false)
     * 4. Create address and wallet
     * 5. Send verification OTP
     */
    @Transactional
    public String register(UserRegistrationDto dto) {
        log.info("Registering new user with email: {}", dto.getEmail());
        
        // Check email uniqueness in our database
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new com.elocate.elocate.exception.AccountAlreadyExistsException("Email already registered: " + dto.getEmail());
        }
        
        // Check mobile number uniqueness in our database
        if (userRepository.existsByMobileNumber(dto.getMobileNumber())) {
            throw new com.elocate.elocate.exception.DuplicateMobileNumberException("Mobile number already registered: " + dto.getMobileNumber());
        }

        String firebaseUid;
        try {
            // Create user in Auth0
            firebaseUid = auth0Service.createUser(dto.getEmail(), dto.getPassword(), dto.getEmail());
            log.info("Created Auth0 user with ID: {}", firebaseUid);
            
        } catch (Exception e) {
            log.error("Failed to create Auth0 user: {}", e.getMessage());
            if (e.getMessage().contains("User already exists") || e.getMessage().contains("email_exists") || e.getMessage().contains("already exists")) {
                throw new com.elocate.elocate.exception.AccountAlreadyExistsException("Email already registered in Auth0");
            }
            throw new RuntimeException("Auth0 registration failed: " + e.getMessage());
        }
        
        // Create user in our database (email not verified yet)
        User user = User.builder()
                .fullName(dto.getFullName())
                .mobileNumber(dto.getMobileNumber())
                .email(dto.getEmail())
                .passwordHash("AUTH0_MANAGED") // Password managed by Auth0
                .firebaseUid(firebaseUid)
                .isEmailVerified(false)
                .isActive(true)
                .role(dto.getRole() != null && !dto.getRole().isEmpty() 
                        ? dto.getRole().toUpperCase() 
                        : com.elocate.elocate.model.enums.UserRole.CITIZEN.name())
                .build();
        User savedUser = userRepository.save(user);
        log.info("Created user in database with ID: {}", savedUser.getId());
        
        // Create address
        UserAddress address = UserAddress.builder()
                .userId(savedUser.getId())
                .address(dto.getAddress())
                .city(dto.getCity())
                .state(dto.getState())
                .pincode(dto.getPincode())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .isDefault(true)
                .build();
        userAddressRepository.save(address);
        log.info("Created address for user: {}", savedUser.getId());
        
        // Create wallet
        UserWallet wallet = UserWallet.builder()
                .userId(savedUser.getId())
                .pointsBalance(BigDecimal.valueOf(0))
                .build();
        userWalletRepository.save(wallet);
        log.info("Created wallet for user: {}", savedUser.getId());
        
        // Auto-verify email (OTP disabled for now)
        // savedUser.setIsEmailVerified(true);
        // userRepository.save(savedUser);
        // log.info("Auto-verified email for user: {}", savedUser.getId());
        
        OTP sending disabled - users are auto-verified
        otpService.generateAndSendOtp(dto.getEmail(), OtpType.EMAIL_VERIFICATION);
        
        return "Registration successful! You can now login with your credentials.";
    }
    
    /**
     * Format phone number for Firebase (must be in E.164 format)
     */
    private String formatPhoneNumber(String mobileNumber) {
        // Remove any non-digit characters
        String digits = mobileNumber.replaceAll("[^0-9]", "");
        
        // If it doesn't start with +, assume it's an Indian number
        if (!mobileNumber.startsWith("+")) {
            // If it starts with country code already, just add +
            if (digits.length() > 10) {
                return "+" + digits;
            }
            // Otherwise add +91 for India
            return "+91" + digits;
        }
        
        return mobileNumber;
    }
}
