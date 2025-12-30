package com.elocate.elocate.repository;

import com.elocate.elocate.model.Otp;
import com.elocate.elocate.model.enums.OtpType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OtpRepository extends JpaRepository<Otp, UUID> {
    
    /**
     * Find valid OTP for email and type (not used and not expired)
     * 
     * @param email Email address
     * @param otpType OTP type
     * @param now Current time to check expiry
     * @return Optional containing OTP if found
     */
    @Query("SELECT o FROM Otp o WHERE o.email = :email AND o.otpType = :otpType " +
           "AND o.isUsed = false AND o.expiresAt > :now ORDER BY o.createdAt DESC")
    Optional<Otp> findValidOtp(String email, OtpType otpType, LocalDateTime now);
    
    /**
     * Mark all unused OTPs for email and type as used
     * 
     * @param email Email address
     * @param otpType OTP type
     */
    @Modifying
    @Query("UPDATE Otp o SET o.isUsed = true WHERE o.email = :email " +
           "AND o.otpType = :otpType AND o.isUsed = false")
    void invalidateOldOtps(String email, OtpType otpType);
}
