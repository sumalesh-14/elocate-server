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
 * Service for user registration with Firebase and email verification
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
     * Register new user with Firebase and email verification
     * 
     * Flow:
     * 1. Check email uniqueness in database
     * 2. Create user in Firebase Auth
     * 3. Create user in our database (email_verified = false)
     * 4. Create address and wallet
     * 5. Send verification OTP
     */
    @Transactional
    public String register(UserRegistrationDto dto) {
        log.info("Registering new user with email: {}", dto.getEmail());
        
        // Check email uniqueness in our database
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + dto.getEmail());
        }

        String firebaseUid;
        try {
            // Create user in Auth0
            // We use the email as username or allow Auth0 to generate one? 
            // signUp takes (email, username, password, connection). We can pass email as username if needed, or null/empty if handled by connection.
            // Using email as username for now.
            firebaseUid = auth0Service.createUser(dto.getEmail(), dto.getPassword(), dto.getEmail());
            log.info("Created Auth0 user with ID: {}", firebaseUid);
            
        } catch (Exception e) {
            log.error("Failed to create Auth0 user: {}", e.getMessage());
            if (e.getMessage().contains("User already exists") || e.getMessage().contains("email_exists")) {
                throw new IllegalArgumentException("Email already registered in Auth0");
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
        
        // Send OTP for email verification
        otpService.generateAndSendOtp(dto.getEmail(), OtpType.EMAIL_VERIFICATION);
        
        return "Registration successful! Please check your email for verification OTP.";
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
