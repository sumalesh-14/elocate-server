package com.elocate.elocate.service;

import com.elocate.elocate.model.enums.OtpType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Service for sending emails
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {
    
    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    /**
     * Send OTP email
     * 
     * @param toEmail Recipient email
     * @param otpCode OTP code
     * @param otpType Type of OTP
     */
    public void sendOtpEmail(String toEmail, String otpCode, OtpType otpType) {
        log.info("Sending OTP email to: {}, type: {}", toEmail, otpType);
        
        String subject = getEmailSubject(otpType);
        String body = getEmailBody(otpCode, otpType);
        
        sendEmail(toEmail, subject, body);
    }
    
    /**
     * Get email subject based on OTP type
     */
    private String getEmailSubject(OtpType otpType) {
        return switch (otpType) {
            case EMAIL_VERIFICATION -> "Verify Your Email - ELocate";
            case PASSWORD_RESET -> "Password Reset Request - ELocate";
            case EMAIL_CHANGE -> "Verify Your New Email - ELocate";
        };
    }
    
    /**
     * Get email body based on OTP type
     */
    private String getEmailBody(String otpCode, OtpType otpType) {
        String baseMessage = switch (otpType) {
            case EMAIL_VERIFICATION -> 
                "Welcome to ELocate!\n\n" +
                "Thank you for registering. Please verify your email address using the OTP below:\n\n";
            case PASSWORD_RESET -> 
                "Password Reset Request\n\n" +
                "We received a request to reset your password. Use the OTP below to reset your password:\n\n";
            case EMAIL_CHANGE -> 
                "Email Change Verification\n\n" +
                "We received a request to change your email address. Use the OTP below to verify your new email:\n\n";
        };
        
        return baseMessage +
               "Your OTP: " + otpCode + "\n\n" +
               "This OTP will expire in 10 minutes.\n\n" +
               "If you didn't request this, please ignore this email.\n\n" +
               "Best regards,\n" +
               "ELocate Team";
    }
    
    /**
     * Send password reset email with Firebase reset link
     * 
     * @param toEmail Recipient email
     * @param resetLink Firebase password reset link
     */
    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        log.info("Sending password reset email to: {}", toEmail);
        
        String subject = "Password Reset Request - ELocate";
        String body = "Password Reset Request\n\n" +
                     "We received a request to reset your password for your ELocate account.\n\n" +
                     "Click the link below to reset your password:\n\n" +
                     resetLink + "\n\n" +
                     "This link will expire in 1 hour.\n\n" +
                     "If you didn't request this password reset, please ignore this email. " +
                     "Your password will remain unchanged.\n\n" +
                     "Best regards,\n" +
                     "ELocate Team";
        
        sendEmail(toEmail, subject, body);
    }
    
    /**
     * Send simple email
     */
    private void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            
            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to: {}, error: {}", to, e.getMessage());
            // Don't throw exception - registration should still succeed even if email fails
            // In production, you might want to queue this for retry
        }
    }
}
