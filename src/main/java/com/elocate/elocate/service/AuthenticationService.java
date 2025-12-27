package com.elocate.elocate.service;

import com.elocate.elocate.dto.FirebaseSignInResponse;
import com.elocate.elocate.dto.LoginRequestDto;
import com.elocate.elocate.dto.UserProfileResponse;
import com.elocate.elocate.model.OtpType;
import com.elocate.elocate.model.User;
import com.elocate.elocate.model.UserAddress;
import com.elocate.elocate.model.UserWallet;
import com.elocate.elocate.repository.UserAddressRepository;
import com.elocate.elocate.repository.UserRepository;
import com.elocate.elocate.repository.UserWalletRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service for Firebase authentication operations
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationService {
    
    private final UserRepository userRepository;
    private final UserAddressRepository userAddressRepository;
    private final UserWalletRepository userWalletRepository;
    private final OtpService otpService;
    private final EmailService emailService;
    private final FirebaseAuth firebaseAuth;
    private final AuthService authService;  // Your existing Firebase login service
    
    /**
     * Login with email and password using Firebase REST API
     * 
     * @param dto Login credentials
     * @return User profile with Firebase ID token
     */
    @Transactional(readOnly = true)
    public UserProfileResponse login(LoginRequestDto dto) {
        log.info("Login attempt for email: {}", dto.getEmail());
        
        // Authenticate with Firebase using your existing AuthService
        // This verifies email + password via Firebase REST API
        FirebaseSignInResponse firebaseResponse = authService.login(dto.getEmail(), dto.getPassword());
        
        log.info("Firebase authentication successful for: {}", dto.getEmail());
        
        // Find user in our database
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found in database"));
        
        // Check if email is verified
        if (!user.getIsEmailVerified()) {
            log.warn("Login attempt with unverified email: {}", dto.getEmail());
            throw new IllegalArgumentException("Email not verified. Please verify your email first.");
        }
        
        // Check if account is active
        if (!user.getIsActive()) {
            throw new IllegalArgumentException("Account is inactive. Please contact support.");
        }
        
        log.info("Login successful for user: {}", user.getId());
        
        // Get address and wallet
        UserAddress address = userAddressRepository.findByUserIdAndIsDefault(user.getId(), true)
                .orElseThrow(() -> new IllegalArgumentException("User address not found"));
        
        UserWallet wallet = userWalletRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));
        
        // Build profile response with Firebase token
        UserProfileResponse profile = buildProfileResponse(user, address, wallet);
        profile.setFirebaseToken(firebaseResponse.getIdToken());
        
        return profile;
    }
    
    /**
     * Verify email with OTP
     */
    @Transactional
    public String verifyEmail(String email, String otp) {
        log.info("Verifying email: {}", email);
        
        // Verify OTP
        if (!otpService.verifyOtp(email, otp, OtpType.EMAIL_VERIFICATION)) {
            throw new IllegalArgumentException("Invalid or expired OTP");
        }
        
        // Update user email verified status
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        user.setIsEmailVerified(true);
        userRepository.save(user);
        
        log.info("Email verified for user: {}", user.getId());
        return "Email verified successfully! You can now login.";
    }
    
    /**
     * Request password reset via Firebase
     */
    public String forgotPassword(String email) {
        log.info("Password reset requested for email: {}", email);
        
        try {
            // Check if user exists in our database
            if (!userRepository.existsByEmail(email)) {
                throw new IllegalArgumentException("Email not registered");
            }
            
            // Generate Firebase password reset link
            String resetLink = firebaseAuth.generatePasswordResetLink(email);
            
            // Send password reset link via email
            emailService.sendPasswordResetEmail(email, resetLink);
            
            log.info("Password reset email sent to: {}", email);
            
            return "Password reset email sent. Please check your inbox.";
            
        } catch (FirebaseAuthException e) {
            log.error("Failed to generate password reset link: {}", e.getMessage());
            throw new RuntimeException("Failed to send password reset email");
        }
    }
    
    /**
     * Request email change with OTP
     */
    public String requestEmailChange(UUID userId, String newEmail) {
        log.info("Email change requested for user: {}, new email: {}", userId, newEmail);
        
        // Check if new email already exists
        if (userRepository.existsByEmail(newEmail)) {
            throw new IllegalArgumentException("Email already in use");
        }
        
        // Send OTP to new email
        otpService.generateAndSendOtp(newEmail, OtpType.EMAIL_CHANGE);
        
        return "OTP sent to new email address";
    }
    
    /**
     * Verify email change with OTP and update Firebase
     */
    @Transactional
    public String verifyEmailChange(UUID userId, String newEmail, String otp) {
        log.info("Verifying email change for user: {}", userId);
        
        // Verify OTP
        if (!otpService.verifyOtp(newEmail, otp, OtpType.EMAIL_CHANGE)) {
            throw new IllegalArgumentException("Invalid or expired OTP");
        }
        
        // Get user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        try {
            // Update email in Firebase
            UserRecord.UpdateRequest request = new UserRecord.UpdateRequest(user.getFirebaseUid())
                    .setEmail(newEmail);
            firebaseAuth.updateUser(request);
            
            // Update email in our database
            user.setEmail(newEmail);
            user.setIsEmailVerified(true);
            userRepository.save(user);
            
            log.info("Email changed successfully for user: {}", userId);
            return "Email changed successfully";
            
        } catch (FirebaseAuthException e) {
            log.error("Failed to update email in Firebase: {}", e.getMessage());
            throw new RuntimeException("Failed to update email");
        }
    }
    
    /**
     * Build profile response
     */
    private UserProfileResponse buildProfileResponse(User user, UserAddress address, UserWallet wallet) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .mobileNumber(user.getMobileNumber())
                .email(user.getEmail())
                .address(UserProfileResponse.AddressInfo.builder()
                        .id(address.getId())
                        .address(address.getAddress())
                        .city(address.getCity())
                        .state(address.getState())
                        .pincode(address.getPincode())
                        .latitude(address.getLatitude())
                        .longitude(address.getLongitude())
                        .build())
                .wallet(UserProfileResponse.WalletInfo.builder()
                        .pointsBalance(wallet.getPointsBalance())
                        .build())
                .build();
    }
}
