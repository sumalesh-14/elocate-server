package com.elocate.elocate.service;

import com.elocate.elocate.dto.*;
import com.elocate.elocate.model.*;
import com.elocate.elocate.model.enums.OtpType;
import com.elocate.elocate.repository.*;
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
        private final RecyclingFacilityRepository facilityRepository;
        private final NotificationPreferencesRepository notifPrefsRepository;
        private final OtpService otpService;
        private final Auth0Service auth0Service;

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

                // Fetch facility for PARTNER/INTERMEDIARY
                RecyclingFacility facility = facilityRepository.findByUserId(userId).orElse(null);

                // Address: citizens have user_address; PARTNER users may not — fall back to facility address
                UserAddress address = userAddressRepository.findByUserIdAndIsDefault(userId, true)
                                .orElse(null);

                // For PARTNER with no user_address, synthesise one from the facility row
                if (address == null && facility != null) {
                        UserAddress a = new UserAddress();
                        a.setAddress(facility.getAddress());
                        a.setCity(facility.getCity());
                        a.setState(facility.getState());
                        a.setPincode(facility.getPincode());
                        a.setLatitude(facility.getLatitude());
                        a.setLongitude(facility.getLongitude());
                        address = a;
                }

                UserWallet wallet = userWalletRepository.findById(userId).orElse(null);

                return buildProfileResponse(user, address, wallet, facility);
        }

        /**
         * Update user profile (user details and address)
         * 
         * @param userId User ID
         * @param dto    Update profile data
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
                // user.setEmail(dto.getEmail());
                User updatedUser = userRepository.save(user);

                // Get and update address — create one if it doesn't exist (PARTNER case)
                UserAddress address = userAddressRepository.findByUserIdAndIsDefault(userId, true)
                                .orElseGet(() -> {
                                        UserAddress a = new UserAddress();
                                        a.setUserId(userId);
                                        a.setIsDefault(true);
                                        return a;
                                });

                address.setAddress(dto.getAddress());
                address.setCity(dto.getCity());
                address.setState(dto.getState());
                address.setPincode(dto.getPincode());
                address.setLatitude(dto.getLatitude());
                address.setLongitude(dto.getLongitude());
                UserAddress updatedAddress = userAddressRepository.save(address);

                UserWallet wallet = userWalletRepository.findById(userId).orElse(null);

                log.info("Profile updated successfully for user: {}", userId);
                RecyclingFacility facility = facilityRepository.findByUserId(userId).orElse(null);
                return buildProfileResponse(updatedUser, updatedAddress, wallet, facility);
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

        /** Request mobile number change — sends OTP to the user's current verified email */
        @Transactional
        public String requestMobileChange(UUID userId, String newMobile) {
                if (userRepository.existsByMobileNumber(newMobile)) {
                        throw new IllegalArgumentException("Mobile number already in use");
                }
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new IllegalArgumentException("User not found"));
                otpService.generateAndSendOtp(user.getEmail(), OtpType.MOBILE_CHANGE);
                return "OTP sent to your registered email address";
        }

        /** Verify OTP and apply mobile number change */
        @Transactional
        public String verifyMobileChange(UUID userId, String newMobile, String otp) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new IllegalArgumentException("User not found"));
                if (!otpService.verifyOtp(user.getEmail(), otp, OtpType.MOBILE_CHANGE)) {
                        throw new IllegalArgumentException("Invalid or expired OTP");
                }
                if (userRepository.existsByMobileNumber(newMobile)) {
                        throw new IllegalArgumentException("Mobile number already in use");
                }
                user.setMobileNumber(newMobile);
                userRepository.save(user);
                return "Mobile number updated successfully";
        }

        /** Update facility profile fields (name, operating hours, address) */
        @Transactional
        public void updateFacilityProfile(UUID userId, UpdateFacilityProfileDto dto) {
                RecyclingFacility facility = facilityRepository.findByUserId(userId)
                                .orElseThrow(() -> new IllegalArgumentException("Facility not found for user"));
                if (dto.getFacilityName() != null && !dto.getFacilityName().isBlank())
                        facility.setName(dto.getFacilityName());
                if (dto.getOperatingHours() != null) facility.setOperatingHours(dto.getOperatingHours());
                if (dto.getAddress() != null && !dto.getAddress().isBlank()) facility.setAddress(dto.getAddress());
                if (dto.getCity() != null) facility.setCity(dto.getCity());
                if (dto.getState() != null && !dto.getState().isBlank()) facility.setState(dto.getState());
                if (dto.getPincode() != null && !dto.getPincode().isBlank()) facility.setPincode(dto.getPincode());
                if (dto.getCapacity() != null) facility.setCapacity(dto.getCapacity());
                facilityRepository.save(facility);
                // Also update owner's full name in user table
                if (dto.getOwnerName() != null && !dto.getOwnerName().isBlank()) {
                        userRepository.findById(userId).ifPresent(u -> {
                                u.setFullName(dto.getOwnerName());
                                userRepository.save(u);
                        });
                }
        }

        /** Change password via Auth0 — verifies current password first, then sends reset email */
        @Transactional
        public String changePassword(UUID userId, ChangePasswordDto dto) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new IllegalArgumentException("User not found"));
                try {
                        auth0Service.login(user.getEmail(), dto.getCurrentPassword());
                } catch (Exception e) {
                        throw new IllegalArgumentException("Current password is incorrect");
                }
                auth0Service.resetPassword(user.getEmail());
                return "Password reset email sent to your registered email address";
        }

        /** Soft-delete account — deactivates user and facility */
        @Transactional
        public String deleteAccount(UUID userId) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new IllegalArgumentException("User not found"));
                user.setIsActive(false);
                userRepository.save(user);
                facilityRepository.findByUserId(userId).ifPresent(f -> {
                        f.setIsActive(false);
                        facilityRepository.save(f);
                });
                return "Account deactivated successfully";
        }

        /** Get notification preferences (creates defaults if not exist) */
        @Transactional
        public NotificationPreferencesDto getNotificationPreferences(UUID userId) {
                NotificationPreferences prefs = notifPrefsRepository.findById(userId)
                                .orElseGet(() -> notifPrefsRepository.save(
                                                NotificationPreferences.builder().userId(userId).build()));
                return toDto(prefs);
        }

        /** Save notification preferences */
        @Transactional
        public NotificationPreferencesDto updateNotificationPreferences(UUID userId, NotificationPreferencesDto dto) {
                NotificationPreferences prefs = notifPrefsRepository.findById(userId)
                                .orElseGet(() -> NotificationPreferences.builder().userId(userId).build());
                if (dto.getNewRequests() != null) prefs.setNewRequests(dto.getNewRequests());
                if (dto.getDailySummary() != null) prefs.setDailySummary(dto.getDailySummary());
                if (dto.getWeeklyReport() != null) prefs.setWeeklyReport(dto.getWeeklyReport());
                if (dto.getMarketing() != null) prefs.setMarketing(dto.getMarketing());
                return toDto(notifPrefsRepository.save(prefs));
        }

        private NotificationPreferencesDto toDto(NotificationPreferences p) {
                return NotificationPreferencesDto.builder()
                                .newRequests(p.getNewRequests())
                                .dailySummary(p.getDailySummary())
                                .weeklyReport(p.getWeeklyReport())
                                .marketing(p.getMarketing())
                                .build();
        }

        /**
         * Build profile response from entities
         */
        private UserProfileResponse buildProfileResponse(
                        User user, UserAddress address, UserWallet wallet, RecyclingFacility facility) {

                return UserProfileResponse.builder()
                                .status("success")
                                .message("Profile fetched successfully")
                                .user(UserProfileResponse.UserData.builder()
                                                .id(user.getId())
                                                .fullName(user.getFullName())
                                                .mobileNumber(user.getMobileNumber())
                                                .email(user.getEmail())
                                                .role(user.getRole())
                                                .facilityId(facility != null ? facility.getId() : null)
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
                                .facility(facility != null ? UserProfileResponse.FacilityData.builder()
                                                .id(facility.getId())
                                                .facilityName(facility.getName())
                                                .operatingHours(facility.getOperatingHours())
                                                .address(facility.getAddress())
                                                .city(facility.getCity())
                                                .state(facility.getState())
                                                .pincode(facility.getPincode())
                                                .latitude(facility.getLatitude())
                                                .longitude(facility.getLongitude())
                                                .registrationNumber(facility.getRegistrationNumber())
                                                .build() : null)
                                .wallet(wallet != null ? UserProfileResponse.WalletData.builder()
                                                .pointsBalance(wallet.getPointsBalance())
                                                .build() : null)
                                .tokens(null)
                                .build();
        }
}
