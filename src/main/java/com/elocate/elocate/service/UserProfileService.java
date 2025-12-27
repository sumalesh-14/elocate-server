package com.elocate.elocate.service;

import com.elocate.elocate.dto.UpdateProfileDto;
import com.elocate.elocate.dto.UserProfileResponse;
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

import java.util.UUID;

/**
 * Service for user profile management
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserProfileService {
    
    private final UserRepository userRepository;
    private final UserAddressRepository userAddressRepository;
    private final UserWalletRepository userWalletRepository;
    
    /**
     * Get complete user profile
     * 
     * @param userId User ID
     * @return Complete profile with user, address, and wallet info
     * @throws IllegalArgumentException if user not found
     */
    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(UUID userId) {
        log.info("Fetching profile for user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        // Get user's address (should always have exactly one default address)
        UserAddress address = userAddressRepository.findByUserIdAndIsDefault(userId, true)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User address not found for user: " + userId));
        
        // Get wallet
        UserWallet wallet = userWalletRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Wallet not found for user: " + userId));
        
        return buildProfileResponse(user, address, wallet);
    }
    
    /**
     * Update user profile (user details and address)
     * 
     * @param userId User ID
     * @param dto Update profile data
     * @return Updated profile
     * @throws IllegalArgumentException if user not found
     */
    @Transactional
    public UserProfileResponse updateProfile(UUID userId, UpdateProfileDto dto) {
        log.info("Updating profile for user: {}", userId);
        
        // Get and update user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        user.setFullName(dto.getFullName());
//        user.setEmail(dto.getEmail());
        User updatedUser = userRepository.save(user);
        
        // Get and update address
        UserAddress address = userAddressRepository.findByUserIdAndIsDefault(userId, true)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User address not found for user: " + userId));
        
        address.setAddress(dto.getAddress());
        address.setCity(dto.getCity());
        address.setState(dto.getState());
        address.setPincode(dto.getPincode());
        address.setLatitude(dto.getLatitude());
        address.setLongitude(dto.getLongitude());
        UserAddress updatedAddress = userAddressRepository.save(address);
        
        // Get wallet (no update needed, just for response)
        UserWallet wallet = userWalletRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Wallet not found for user: " + userId));
        
        log.info("Profile updated successfully for user: {}", userId);
        return buildProfileResponse(updatedUser, updatedAddress, wallet);
    }
    
    /**
     * Placeholder for mobile number change with OTP verification
     * TODO: Implement OTP verification flow
     */
    public void changeMobileNumber(UUID userId, String newMobileNumber, String otpCode) {
        log.info("Mobile change requested for user: {}, new number: {}", userId, newMobileNumber);
        // TODO: Implement OTP verification
        throw new UnsupportedOperationException("Mobile number change with OTP not yet implemented");
    }
    
    /**
     * Placeholder for email change with OTP verification
     * TODO: Implement OTP verification flow
     */
    public void changeEmail(UUID userId, String newEmail, String otpCode) {
        log.info("Email change requested for user: {}, new email: {}", userId, newEmail);
        // TODO: Implement OTP verification
        throw new UnsupportedOperationException("Email change with OTP not yet implemented");
    }
    
    /**
     * Build profile response from entities
     */
    private UserProfileResponse buildProfileResponse(
            User user, UserAddress address, UserWallet wallet) {
        
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
