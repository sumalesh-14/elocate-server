package com.elocate.elocate.service;

import com.elocate.elocate.model.enums.OtpType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Service for sending emails
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Async
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.base.url:http://localhost:3000}")
    private String appBaseUrl;

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
     * @param toEmail   Recipient email
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

    // ========== Workflow Notification Methods ==========

    /**
     * Send recycle request created confirmation to citizen
     */
    public void sendRequestCreatedEmail(String toEmail, String requestId, String deviceName,
            BigDecimal estimatedAmount) {
        log.info("Sending request created email to: {}", toEmail);

        String subject = "Recycle Request Created - ELocate";
        String body = "Your Recycle Request Has Been Created\n\n" +
                "Thank you for choosing ELocate for your e-waste recycling!\n\n" +
                "Request ID: " + requestId + "\n" +
                "Device: " + deviceName + "\n" +
                "Estimated Payment: $" + estimatedAmount + "\n\n" +
                "Your request has been sent to the recycling facility for approval.\n" +
                "You will receive an email once it's approved.\n\n" +
                "Track your request: " + appBaseUrl + "/citizen/recycle\n\n" +
                "Best regards,\n" +
                "ELocate Team";

        sendEmail(toEmail, subject, body);
    }

    /**
     * Send request assigned notification to intermediary
     */
    public void sendRequestAssignedToFacilityEmail(String toEmail, String requestId, String deviceName) {
        log.info("Sending request assigned email to facility: {}", toEmail);

        String subject = "New Recycle Request Assigned - ELocate";
        String body = "New Recycle Request Assigned to Your Facility\n\n" +
                "Request ID: " + requestId + "\n" +
                "Device: " + deviceName + "\n\n" +
                "Please review and approve this request in your dashboard.\n\n" +
                "View request: " + appBaseUrl + "/intermediary/collections\n\n" +
                "Best regards,\n" +
                "ELocate Team";

        sendEmail(toEmail, subject, body);
    }

    /**
     * Send request approved notification to citizen
     */
    public void sendRequestApprovedEmail(String toEmail, String requestId, BigDecimal approvedAmount) {
        log.info("Sending request approved email to: {}", toEmail);

        String subject = "Recycle Request Approved - ELocate";
        String body = "Your Recycle Request Has Been Approved!\n\n" +
                "Request ID: " + requestId + "\n" +
                "Approved Payment: $" + approvedAmount + "\n\n" +
                "Your request has been approved. The facility will schedule a pickup or " +
                "you can drop off your device at the facility.\n\n" +
                "Track your request: " + appBaseUrl + "/citizen/recycle\n\n" +
                "Best regards,\n" +
                "ELocate Team";

        sendEmail(toEmail, subject, body);
    }

    /**
     * Send driver assignment notification to driver
     */
    public void sendDriverAssignmentEmail(String toEmail, String driverName, String requestId,
            String citizenAddress, String pickupToken) {
        log.info("Sending driver assignment email to: {}", toEmail);

        String subject = "Pickup Assignment - ELocate";
        String body = "Hello " + driverName + ",\n\n" +
                "You have been assigned a new pickup:\n\n" +
                "Request ID: " + requestId + "\n" +
                "Pickup Address: " + citizenAddress + "\n\n" +
                "After completing the pickup, please use one of these links:\n\n" +
                "✅ Pickup Completed:\n" +
                appBaseUrl + "/api/v1/driver/pickup/" + requestId + "/complete?token=" + pickupToken + "\n\n" +
                "❌ Pickup Failed:\n" +
                appBaseUrl + "/api/v1/driver/pickup/" + requestId + "/fail?token=" + pickupToken + "\n\n" +
                "Best regards,\n" +
                "ELocate Team";

        sendEmail(toEmail, subject, body);
    }

    /**
     * Send pickup scheduled notification to citizen
     */
    public void sendPickupScheduledEmail(String toEmail, String requestId, String pickupDate,
            String driverName, String driverPhone) {
        log.info("Sending pickup scheduled email to: {}", toEmail);

        String subject = "Pickup Scheduled - ELocate";
        String body = "Your Pickup Has Been Scheduled\n\n" +
                "Request ID: " + requestId + "\n" +
                "Pickup Date: " + pickupDate + "\n" +
                "Driver: " + driverName + "\n" +
                "Driver Phone: " + driverPhone + "\n\n" +
                "Please ensure someone is available at the pickup address.\n\n" +
                "Track your request: " + appBaseUrl + "/citizen/recycle\n\n" +
                "Best regards,\n" +
                "ELocate Team";

        sendEmail(toEmail, subject, body);
    }

    /**
     * Send pickup completed notification
     */
    public void sendPickupCompletedEmail(String toEmail, String requestId) {
        log.info("Sending pickup completed email to: {}", toEmail);

        String subject = "Pickup Completed - ELocate";
        String body = "Your Device Has Been Picked Up\n\n" +
                "Request ID: " + requestId + "\n\n" +
                "Your device has been successfully picked up and is on its way to the recycling facility.\n" +
                "You will receive another email once the recycling is complete and payment is processed.\n\n" +
                "Track your request: " + appBaseUrl + "/citizen/recycle\n\n" +
                "Best regards,\n" +
                "ELocate Team";

        sendEmail(toEmail, subject, body);
    }

    /**
     * Send pickup failed notification
     */
    public void sendPickupFailedEmail(String toEmail, String requestId, String reason) {
        log.info("Sending pickup failed email to: {}", toEmail);

        String subject = "Pickup Failed - ELocate";
        String body = "Pickup Attempt Failed\n\n" +
                "Request ID: " + requestId + "\n" +
                "Reason: " + reason + "\n\n" +
                "Unfortunately, the driver was unable to complete the pickup. " +
                "Our team will contact you to reschedule.\n\n" +
                "Track your request: " + appBaseUrl + "/citizen/recycle\n\n" +
                "Best regards,\n" +
                "ELocate Team";

        sendEmail(toEmail, subject, body);
    }

    /**
     * Send device received at facility notification
     */
    public void sendDeviceReceivedEmail(String toEmail, String requestId) {
        log.info("Sending device received email to: {}", toEmail);

        String subject = "Device Received - ELocate";
        String body = "Your Device Has Been Received\n\n" +
                "Request ID: " + requestId + "\n\n" +
                "The recycling facility has received your device and will begin processing it.\n" +
                "You will receive payment confirmation once recycling is complete.\n\n" +
                "Track your request: " + appBaseUrl + "/citizen/recycle\n\n" +
                "Best regards,\n" +
                "ELocate Team";

        sendEmail(toEmail, subject, body);
    }

    /**
     * Send recycling completed with payment notification
     */
    public void sendRecyclingCompletedEmail(String toEmail, String requestId, BigDecimal finalAmount) {
        log.info("Sending recycling completed email to: {}", toEmail);

        String subject = "Recycling Complete - Payment Credited - ELocate";
        String body = "Your Device Has Been Recycled!\n\n" +
                "Request ID: " + requestId + "\n" +
                "Payment Amount: $" + finalAmount + "\n\n" +
                "Congratulations! Your device has been successfully recycled.\n" +
                "The payment has been credited to your wallet.\n\n" +
                "View your wallet: " + appBaseUrl + "/citizen/profile\n" +
                "Download certificate: " + appBaseUrl + "/citizen/recycle/" + requestId + "\n\n" +
                "Thank you for contributing to a cleaner environment!\n\n" +
                "Best regards,\n" +
                "ELocate Team";

        sendEmail(toEmail, subject, body);
    }

    /**
     * Send price change notification
     */
    public void sendPriceChangeNotification(String toEmail, String requestId, String reason) {
        log.info("Sending price change notification to: {}", toEmail);

        String subject = "Price Updated - ELocate";
        String body = "Your Request Price Has Been Updated\n\n" +
                "Request ID: " + requestId + "\n" +
                "Reason: " + reason + "\n\n" +
                "The pricing for your recycle request has been updated.\n" +
                "Please check your request details for the new amount.\n\n" +
                "View request: " + appBaseUrl + "/citizen/recycle\n\n" +
                "Best regards,\n" +
                "ELocate Team";

        sendEmail(toEmail, subject, body);
    }

    /**
     * Send facility reassignment notification
     */
    public void sendFacilityReassignmentEmail(String toEmail, String requestId, boolean isNewFacility) {
        log.info("Sending facility reassignment email to: {}", toEmail);

        String subject = isNewFacility ? "New Request Assigned - ELocate" : "Request Reassigned - ELocate";
        String body = (isNewFacility ? "A recycle request has been assigned to your facility.\n\n"
                : "A recycle request has been reassigned from your facility.\n\n") +
                "Request ID: " + requestId + "\n\n" +
                "View request: " + appBaseUrl + "/intermediary/collections\n\n" +
                "Best regards,\n" +
                "ELocate Team";

        sendEmail(toEmail, subject, body);
    }

    /**
     * Send facility approval/rejection notification
     */
    public void sendFacilityApprovalEmail(String toEmail, String facilityName, boolean approved, String notes) {
        log.info("Sending facility {} email to: {}", approved ? "approval" : "rejection", toEmail);

        String subject = approved ? "Facility Approved - ELocate" : "Facility Application Update - ELocate";
        String body = (approved ? "Congratulations! Your facility has been approved.\n\n"
                : "Thank you for your interest in ELocate.\n\n") +
                "Facility: " + facilityName + "\n" +
                "Status: " + (approved ? "APPROVED" : "REJECTED") + "\n" +
                (notes != null && !notes.isBlank() ? "Notes: " + notes + "\n" : "") + "\n" +
                (approved ? "You can now start receiving recycle requests.\n" +
                        "Access your dashboard: " + appBaseUrl + "/intermediary\n"
                        : "If you have questions, please contact our support team.\n")
                + "\n" +
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
            // Don't throw exception - operations should still succeed even if email fails
            // In production, you might want to queue this for retry
        }
    }
}
