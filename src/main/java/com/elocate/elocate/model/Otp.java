package com.elocate.elocate.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * OTP entity for storing one-time passwords
 */
@Entity
@Table(name = "otp")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Otp {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false)
    private String email;
    
    @Column(name = "otp_code", nullable = false, length = 6)
    private String otpCode;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "otp_type", nullable = false, length = 50)
    private OtpType otpType;
    
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    @Builder.Default
    @Column(name = "is_used")
    private Boolean isUsed = false;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
