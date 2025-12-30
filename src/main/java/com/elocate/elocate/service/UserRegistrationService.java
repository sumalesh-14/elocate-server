package com.elocate.elocate.service;

import com.elocate.elocate.dto.UserRegistrationDto;
import com.elocate.elocate.model.enums.OtpType;
import com.elocate.elocate.model.User;
import com.elocate.elocate.model.UserAddress;
import com.elocate.elocate.model.UserWallet;
import com.elocate.elocate.repository.UserAddressRepository;
import com.elocate.elocate.repository.UserRepository;
import com.elocate.elocate.repository.UserWalletRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
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
    private final FirebaseAuth firebaseAuth;
    
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
            // Create user in Firebase Authentication
            UserRecord.CreateRequest firebaseRequest = new UserRecord.CreateRequest()
                    .setEmail(dto.getEmail())
                    .setPassword(dto.getPassword())
                    .setDisplayName(dto.getFullName())
                    .setPhoneNumber(formatPhoneNumber(dto.getMobileNumber()))
                    .setEmailVerified(false)
                    .setDisabled(false);
            
            UserRecord userRecord = firebaseAuth.createUser(firebaseRequest);
            firebaseUid = userRecord.getUid();
            log.info("Created Firebase user with UID: {}", firebaseUid);
            
        } catch (FirebaseAuthException e) {
            log.error("Failed to create Firebase user: {}", e.getMessage());
            if (e.getMessage().contains("EMAIL_EXISTS")) {
                throw new IllegalArgumentException("Email already registered in Firebase");
            }
            throw new RuntimeException("Firebase registration failed: " + e.getMessage());
        }
        
        // Create user in our database (email not verified yet)
        User user = User.builder()
                .fullName(dto.getFullName())
                .mobileNumber(dto.getMobileNumber())
                .email(dto.getEmail())
                .passwordHash("FIREBASE_MANAGED") // Password managed by Firebase
                .firebaseUid(firebaseUid)
                .isEmailVerified(false)
                .isActive(true)
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
