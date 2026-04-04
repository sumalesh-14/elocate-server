package com.elocate.elocate.service;

import com.auth0.json.auth.TokenHolder;
import com.elocate.elocate.dto.LoginRequestDto;
import com.elocate.elocate.dto.UserProfileResponse;
import com.elocate.elocate.model.enums.OtpType;
import com.elocate.elocate.model.User;
import com.elocate.elocate.model.UserAddress;
import com.elocate.elocate.model.UserWallet;
import com.elocate.elocate.repository.UserAddressRepository;
import com.elocate.elocate.repository.UserRepository;
import com.elocate.elocate.repository.UserWalletRepository;
import com.elocate.elocate.repository.RecyclingFacilityRepository;
import com.elocate.elocate.model.RecyclingFacility;
import com.elocate.elocate.security.JwtUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service for Auth0 authentication operations
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
    private final Auth0Service auth0Service; // Replace Firebase with Auth0
    private final JwtUtil jwtUtil;
    private final RecyclingFacilityRepository facilityRepository;

    /**
     * Login with email and password using Auth0
     * 
     * @param dto Login credentials
     * @return User profile with Auth0 tokens
     */
    @Transactional(readOnly = true)
    public UserProfileResponse login(LoginRequestDto dto) {
        log.info("Login attempt for email: {}", dto.getEmail());

        // Authenticate with Auth0
        com.auth0.json.auth.TokenHolder auth0Response = auth0Service.login(dto.getEmail(), dto.getPassword());

        // Check if user exists in our database
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found in database"));

        log.info("User found in database - ID: {}, EmailVerified: {}",
                user.getId(), user.getIsEmailVerified());

        // For partners, check approval status before email verification
        if ("PARTNER".equals(user.getRole())) {
            RecyclingFacility facility = facilityRepository.findByUserId(user.getId())
                    .orElse(null);
            
            if (facility != null) {
                String approvalStatus = facility.getApprovalStatus();
                log.info("Partner approval status: {}", approvalStatus);
                
                // Check approval status first
                if ("PENDING".equals(approvalStatus)) {
                    throw new IllegalArgumentException(
                        "Your partner registration is pending approval. You'll receive an email once approved.");
                }
                
                if ("REJECTED".equals(approvalStatus)) {
                    throw new IllegalArgumentException(
                        "Your partner registration was rejected. Please contact support for more information.");
                }
                
                // If approved, continue with normal checks
            }
        }

        // Check if email is verified
        if (Boolean.FALSE.equals(user.getIsEmailVerified())) {
            log.warn("Login attempt with unverified email: {}", dto.getEmail());
            throw new IllegalArgumentException("Email not verified. Please verify your email first.");
        }

        // Check if account is active
        if (Boolean.FALSE.equals(user.getIsActive())) {
            // For partners, check if it's due to approval status
            if ("PARTNER".equals(user.getRole())) {
                RecyclingFacility facility = facilityRepository.findByUserId(user.getId())
                        .orElse(null);
                
                if (facility != null) {
                    String approvalStatus = facility.getApprovalStatus();
                    
                    if ("PENDING".equals(approvalStatus)) {
                        throw new IllegalArgumentException(
                            "Your partner registration is pending approval. You'll receive an email once approved.");
                    }
                    
                    if ("REJECTED".equals(approvalStatus)) {
                        throw new IllegalArgumentException(
                            "Your partner registration was rejected. Please contact support for more information.");
                    }
                }
                
                // If not related to approval, it's a deactivated account
                throw new IllegalArgumentException(
                    "Your partner account has been deactivated. Please contact support.");
            }
            
            throw new IllegalArgumentException("Account is inactive. Please contact support.");
        }

        log.info("Login successful for user: {}", user.getId());

        // Generate JWT token for API authentication
        String jwtToken = jwtUtil.generateToken(user.getId(), user.getFullName(), user.getEmail(), user.getRole());

        // Build profile response with structured tokens
        return buildProfileResponse(user, jwtToken, auth0Response.getRefreshToken(), auth0Response.getAccessToken());
    }

    /**
     * Refresh tokens using Firebase refresh token
     */
    /**
     * Refresh tokens using Auth0 refresh token
     */
    @Transactional(readOnly = true)
    public UserProfileResponse refreshToken(String refreshToken) {
        log.info("Token refresh attempt");

        // Exchange refresh token for new tokens with Auth0
        TokenHolder auth0Response = auth0Service.refreshToken(refreshToken);

        // For refresh token, we need user info but can't parse Auth0 ID token with our
        // JWT util
        // Temporary solution: require login again
        throw new IllegalArgumentException("Token refresh not implemented. Please login again.");
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

            // Trigger Auth0 password reset
            auth0Service.resetPassword(email);

            return "Password reset email sent. Please check your inbox.";

        } catch (Exception e) {
            log.error("Failed to trigger password reset: {}", e.getMessage());
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
            // Update email in Auth0 if firebaseUid (auth0 user_id) is present
            if (user.getFirebaseUid() != null) {
                auth0Service.updateUserEmail(user.getFirebaseUid(), newEmail);
                log.info("Auth0 email updated for user: {}", userId);
            }

            // Update email in our database
            user.setEmail(newEmail);
            user.setIsEmailVerified(true);
            userRepository.save(user);

            log.info("Email changed successfully for user: {}", userId);
            return "Email changed successfully";

        } catch (Exception e) {
            log.error("Failed to update email: {}", e.getMessage());
            throw new RuntimeException("Failed to update email: " + e.getMessage());
        }
    }

    /**
     * Build profile response
     */
    /**
     * Build profile response
     */
    private UserProfileResponse buildProfileResponse(User user, String jwtToken, String refreshToken,
            String providerToken) {
        // Fetch user address
        UserAddress address = userAddressRepository.findByUserIdAndIsDefault(user.getId(), true)
                .orElse(null);

        // Fetch wallet
        UserWallet wallet = userWalletRepository.findById(user.getId())
                .orElse(null);

        // Fetch facility ID for intermediary/partner users
        UUID facilityId = null;
        String role = user.getRole();
        if (role != null && (role.equalsIgnoreCase("INTERMEDIARY") || role.equalsIgnoreCase("PARTNER")
                || role.equalsIgnoreCase("ROLE_INTERMEDIARY") || role.equalsIgnoreCase("ROLE_PARTNER"))) {
            facilityId = facilityRepository.findByUserId(user.getId())
                    .map(RecyclingFacility::getId)
                    .orElse(null);
        }

        return UserProfileResponse.builder()
                .status("success")
                .message("Authentication successful")
                .user(UserProfileResponse.UserData.builder()
                        .id(user.getId())
                        .fullName(user.getFullName())
                        .mobileNumber(user.getMobileNumber())
                        .email(user.getEmail())
                        .role(user.getRole())
                        .facilityId(facilityId)
                        .build())
                .tokens(UserProfileResponse.TokenData.builder()
                        .accessToken(jwtToken)
                        .refreshToken(refreshToken)
                        .providerAccessToken(providerToken)
                        .expiresIn(jwtUtil.getExpirationTime())
                        .build())
                .address(address != null ? UserProfileResponse.AddressData.builder()
                        .id(address.getId())
                        .address(address.getAddress())
                        .city(address.getCity())
                        .state(address.getState())
                        .pincode(address.getPincode())
                        .latitude(address.getLatitude())
                        .longitude(address.getLongitude())
                        .build() : null)
                .wallet(wallet != null ? UserProfileResponse.WalletData.builder()
                        .pointsBalance(wallet.getPointsBalance())
                        .build() : null)
                .build();
    }
}
