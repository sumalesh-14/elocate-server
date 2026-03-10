package com.elocate.elocate.service;

import com.elocate.elocate.model.enums.OtpType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for sending emails
 * Supports both SMTP and SendGrid HTTP API with HTML templates
 */
@Service
@Slf4j
@Async
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailTemplateService templateService;

    @Autowired(required = false)
    private SendGridEmailService sendGridEmailService;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.base.url:http://localhost:3000}")
    private String appBaseUrl;

    @Value("${app.email.provider:smtp}")
    private String emailProvider;

    @Value("${app.email.notifications.enabled:true}")
    private boolean notificationsEnabled;

    @Value("${app.email.notifications.request-created:true}")
    private boolean requestCreatedEnabled;

    @Value("${app.email.notifications.request-approved:true}")
    private boolean requestApprovedEnabled;

    @Value("${app.email.notifications.driver-assignment:true}")
    private boolean driverAssignmentEnabled;

    @Value("${app.email.notifications.pickup-scheduled:true}")
    private boolean pickupScheduledEnabled;

    @Value("${app.email.notifications.pickup-completed:true}")
    private boolean pickupCompletedEnabled;

    @Value("${app.email.notifications.pickup-failed:true}")
    private boolean pickupFailedEnabled;

    @Value("${app.email.notifications.device-received:true}")
    private boolean deviceReceivedEnabled;

    @Value("${app.email.notifications.recycling-completed:true}")
    private boolean recyclingCompletedEnabled;

    @Value("${app.email.notifications.facility-assignment:true}")
    private boolean facilityAssignmentEnabled;

    @Value("${app.email.notifications.reminder:true}")
    private boolean reminderEnabled;

    @Value("${app.email.use-html-templates:true}")
    private boolean useHtmlTemplates;

    public EmailService(JavaMailSender mailSender, EmailTemplateService templateService) {
        this.mailSender = mailSender;
        this.templateService = templateService;
    }

    /**
     * Check if email notifications are enabled globally
     */
    private boolean isEmailEnabled() {
        return notificationsEnabled;
    }

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
        if (!isEmailEnabled() || !requestCreatedEnabled) {
            log.info("Request created email disabled, skipping");
            return;
        }
        
        log.info("Sending request created email to: {}", toEmail);

        String subject = "Recycle Request Created - ELocate";
        
        if (useHtmlTemplates && templateService.templateExists("request-created")) {
            Map<String, Object> variables = new HashMap<>();
            variables.put("requestId", requestId);
            variables.put("deviceName", deviceName);
            variables.put("estimatedAmount", estimatedAmount.toString());
            variables.put("trackUrl", appBaseUrl + "/citizen/recycle");
            
            String htmlBody = templateService.processTemplate("request-created", variables);
            sendHtmlEmail(toEmail, subject, htmlBody);
        } else {
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
    }

    /**
     * Send request assigned notification to intermediary
     */
    public void sendRequestAssignedToFacilityEmail(String toEmail, String requestId, String deviceName) {
        if (!isEmailEnabled() || !facilityAssignmentEnabled) {
            log.info("Facility assignment email disabled, skipping");
            return;
        }
        
        log.info("Sending request assigned email to facility: {}", toEmail);

        String subject = "New Recycle Request Assigned - ELocate";
        
        if (useHtmlTemplates && templateService.templateExists("facility-assignment")) {
            Map<String, Object> variables = new HashMap<>();
            variables.put("requestId", requestId);
            variables.put("deviceName", deviceName);
            variables.put("dashboardUrl", appBaseUrl + "/intermediary/collections");
            
            String htmlBody = templateService.processTemplate("facility-assignment", variables);
            sendHtmlEmail(toEmail, subject, htmlBody);
        } else {
            String body = "New Recycle Request Assigned to Your Facility\n\n" +
                    "Request ID: " + requestId + "\n" +
                    "Device: " + deviceName + "\n\n" +
                    "Please review and approve this request in your dashboard.\n\n" +
                    "View request: " + appBaseUrl + "/intermediary/collections\n\n" +
                    "Best regards,\n" +
                    "ELocate Team";
            sendEmail(toEmail, subject, body);
        }
    }

    /**
     * Send request approved notification to citizen
     */
    public void sendRequestApprovedEmail(String toEmail, String requestId, BigDecimal approvedAmount) {
        if (!isEmailEnabled() || !requestApprovedEnabled) {
            log.info("Request approved email disabled, skipping");
            return;
        }
        
        log.info("Sending request approved email to: {}", toEmail);

        String subject = "Recycle Request Approved - ELocate";
        
        if (useHtmlTemplates && templateService.templateExists("request-approved")) {
            Map<String, Object> variables = new HashMap<>();
            variables.put("requestId", requestId);
            variables.put("deviceName", "Your Device");
            variables.put("approvedAmount", approvedAmount.toString());
            variables.put("trackUrl", appBaseUrl + "/citizen/recycle");
            
            String htmlBody = templateService.processTemplate("request-approved", variables);
            sendHtmlEmail(toEmail, subject, htmlBody);
        } else {
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
    }

    /**
     * Send driver assignment notification to driver
     */
    public void sendDriverAssignmentEmail(String toEmail, String driverName, String requestId,
            String citizenAddress, String pickupToken) {
        if (!isEmailEnabled() || !driverAssignmentEnabled) {
            log.info("Driver assignment email disabled, skipping");
            return;
        }
        
        log.info("Sending driver assignment email to: {}", toEmail);

        String subject = "Pickup Assignment - ELocate";
        
        if (useHtmlTemplates && templateService.templateExists("driver-assignment")) {
            Map<String, Object> variables = new HashMap<>();
            variables.put("driverName", driverName);
            variables.put("requestId", requestId);
            variables.put("pickupAddress", citizenAddress);
            variables.put("deviceName", "E-Waste Device");
            variables.put("pickupDate", "As scheduled");
            variables.put("acceptUrl", appBaseUrl + "/driver/pickup/accept/" + pickupToken);
            variables.put("rejectUrl", appBaseUrl + "/driver/pickup/reject/" + pickupToken);
            variables.put("comments", ""); // Will be populated if comments exist
            
            String htmlBody = templateService.processTemplate("driver-assignment", variables);
            sendHtmlEmail(toEmail, subject, htmlBody);
        } else {
            String body = "Hello " + driverName + ",\n\n" +
                    "You have been assigned a new pickup:\n\n" +
                    "Request ID: " + requestId + "\n" +
                    "Pickup Address: " + citizenAddress + "\n\n" +
                    "After completing the pickup, please use one of these links:\n\n" +
                    "✅ Pickup Completed:\n" +
                    appBaseUrl + "/driver/pickup/accept/" + pickupToken + "\n\n" +
                    "❌ Pickup Failed:\n" +
                    appBaseUrl + "/driver/pickup/reject/" + pickupToken + "\n\n" +
                    "Best regards,\n" +
                    "ELocate Team";
            sendEmail(toEmail, subject, body);
        }
    }

    /**
     * Send driver assignment notification with comments
     */
    public void sendDriverAssignmentEmailWithComments(String toEmail, String driverName, String requestId,
            String citizenAddress, String pickupToken, String comments, String deviceName, String pickupDate) {
        if (!isEmailEnabled() || !driverAssignmentEnabled) {
            log.info("Driver assignment email disabled, skipping");
            return;
        }
        
        log.info("Sending driver assignment email with comments to: {}", toEmail);

        String subject = "Pickup Assignment - ELocate";
        
        if (useHtmlTemplates && templateService.templateExists("driver-assignment")) {
            Map<String, Object> variables = new HashMap<>();
            variables.put("driverName", driverName);
            variables.put("requestId", requestId);
            variables.put("pickupAddress", citizenAddress);
            variables.put("deviceName", deviceName != null ? deviceName : "E-Waste Device");
            variables.put("pickupDate", pickupDate != null ? pickupDate : "As scheduled");
            variables.put("acceptUrl", appBaseUrl + "/driver/pickup/accept/" + pickupToken);
            variables.put("rejectUrl", appBaseUrl + "/driver/pickup/reject/" + pickupToken);
            variables.put("comments", comments);
            
            String htmlBody = templateService.processTemplate("driver-assignment", variables);
            sendHtmlEmail(toEmail, subject, htmlBody);
        } else {
            String body = "Hello " + driverName + ",\n\n" +
                    "You have been assigned a new pickup:\n\n" +
                    "Request ID: " + requestId + "\n" +
                    "Pickup Address: " + citizenAddress + "\n" +
                    "Device: " + (deviceName != null ? deviceName : "E-Waste Device") + "\n" +
                    "Pickup Date: " + (pickupDate != null ? pickupDate : "As scheduled") + "\n\n" +
                    (comments != null && !comments.isBlank() ? 
                        "📝 Special Instructions:\n" + comments + "\n\n" : "") +
                    "After completing the pickup, please use one of these links:\n\n" +
                    "✅ Pickup Completed:\n" +
                    appBaseUrl + "/driver/pickup/accept/" + pickupToken + "\n\n" +
                    "❌ Pickup Failed:\n" +
                    appBaseUrl + "/driver/pickup/reject/" + pickupToken + "\n\n" +
                    "Best regards,\n" +
                    "ELocate Team";
            sendEmail(toEmail, subject, body);
        }
    }

    /**
     * Send pickup scheduled notification to citizen
     */
    public void sendPickupScheduledEmail(String toEmail, String requestId, String pickupDate,
            String driverName, String driverPhone) {
        if (!isEmailEnabled() || !pickupScheduledEnabled) {
            log.info("Pickup scheduled email disabled, skipping");
            return;
        }
        
        log.info("Sending pickup scheduled email to: {}", toEmail);

        String subject = "Pickup Scheduled - ELocate";
        
        if (useHtmlTemplates && templateService.templateExists("pickup-scheduled")) {
            Map<String, Object> variables = new HashMap<>();
            variables.put("requestId", requestId);
            variables.put("deviceName", "Your E-Waste Device");
            variables.put("pickupDate", pickupDate);
            variables.put("pickupAddress", "Your registered address");
            variables.put("driverName", driverName);
            variables.put("driverPhone", driverPhone);
            
            String htmlBody = templateService.processTemplate("pickup-scheduled", variables);
            sendHtmlEmail(toEmail, subject, htmlBody);
        } else {
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
    }

    /**
     * Send pickup completed notification
     */
    public void sendPickupCompletedEmail(String toEmail, String requestId) {
        if (!isEmailEnabled() || !pickupCompletedEnabled) {
            log.info("Pickup completed email disabled, skipping");
            return;
        }
        
        log.info("Sending pickup completed email to: {}", toEmail);

        String subject = "Pickup Completed - ELocate";
        
        if (useHtmlTemplates && templateService.templateExists("pickup-completed")) {
            Map<String, Object> variables = new HashMap<>();
            variables.put("requestId", requestId);
            
            String htmlBody = templateService.processTemplate("pickup-completed", variables);
            sendHtmlEmail(toEmail, subject, htmlBody);
        } else {
            String body = "Your Device Has Been Picked Up\n\n" +
                    "Request ID: " + requestId + "\n\n" +
                    "Your device has been successfully picked up and is on its way to the recycling facility.\n" +
                    "You will receive another email once the recycling is complete and payment is processed.\n\n" +
                    "Track your request: " + appBaseUrl + "/citizen/recycle\n\n" +
                    "Best regards,\n" +
                    "ELocate Team";
            sendEmail(toEmail, subject, body);
        }
    }

    /**
     * Send pickup failed notification
     */
    public void sendPickupFailedEmail(String toEmail, String requestId, String reason) {
        if (!isEmailEnabled() || !pickupFailedEnabled) {
            log.info("Pickup failed email disabled, skipping");
            return;
        }
        
        log.info("Sending pickup failed email to: {}", toEmail);

        String subject = "Pickup Failed - ELocate";
        
        if (useHtmlTemplates && templateService.templateExists("pickup-failed")) {
            Map<String, Object> variables = new HashMap<>();
            variables.put("requestId", requestId);
            variables.put("reason", reason != null ? reason : "Not specified");
            
            String htmlBody = templateService.processTemplate("pickup-failed", variables);
            sendHtmlEmail(toEmail, subject, htmlBody);
        } else {
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
    }

    /**
     * Send device received at facility notification
     */
    public void sendDeviceReceivedEmail(String toEmail, String requestId) {
        if (!isEmailEnabled() || !deviceReceivedEnabled) {
            log.info("Device received email disabled, skipping");
            return;
        }
        
        log.info("Sending device received email to: {}", toEmail);

        String subject = "Device Received - ELocate";
        
        if (useHtmlTemplates && templateService.templateExists("device-received")) {
            Map<String, Object> variables = new HashMap<>();
            variables.put("requestId", requestId);
            
            String htmlBody = templateService.processTemplate("device-received", variables);
            sendHtmlEmail(toEmail, subject, htmlBody);
        } else {
            String body = "Your Device Has Been Received\n\n" +
                    "Request ID: " + requestId + "\n\n" +
                    "The recycling facility has received your device and will begin processing it.\n" +
                    "You will receive payment confirmation once recycling is complete.\n\n" +
                    "Track your request: " + appBaseUrl + "/citizen/recycle\n\n" +
                    "Best regards,\n" +
                    "ELocate Team";
            sendEmail(toEmail, subject, body);
        }
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
     * Send recycling completed with certificate notification
     */
    public void sendRecyclingCompletedWithCertificateEmail(String toEmail, String requestId, 
            BigDecimal finalAmount, String certificateUrl) {
        if (!isEmailEnabled() || !recyclingCompletedEnabled) {
            log.info("Recycling completed email disabled, skipping");
            return;
        }
        
        log.info("Sending recycling completed with certificate email to: {}", toEmail);

        String subject = "🎉 Recycling Complete - Certificate & Payment - ELocate";
        
        if (useHtmlTemplates && templateService.templateExists("recycling-completed")) {
            Map<String, Object> variables = new HashMap<>();
            variables.put("requestId", requestId);
            variables.put("deviceName", "Your E-Waste Device");
            variables.put("finalAmount", finalAmount.toString());
            variables.put("certificateUrl", certificateUrl);
            
            String htmlBody = templateService.processTemplate("recycling-completed", variables);
            sendHtmlEmail(toEmail, subject, htmlBody);
        } else {
            String body = "Your Device Has Been Recycled!\n\n" +
                    "Request ID: " + requestId + "\n" +
                    "Payment Amount: $" + finalAmount + "\n\n" +
                    "Congratulations! Your device has been successfully recycled.\n" +
                    "The payment has been credited to your wallet.\n\n" +
                    "🏆 YOUR CERTIFICATE OF APPRECIATION:\n" +
                    certificateUrl + "\n\n" +
                    "View your wallet: " + appBaseUrl + "/citizen/profile\n" +
                    "View all certificates: " + appBaseUrl + "/citizen/recycle\n\n" +
                    "Thank you for being an environmental champion and contributing to a cleaner planet!\n" +
                    "Your action makes a real difference. 🌍💚\n\n" +
                    "Best regards,\n" +
                    "ELocate Team";
            sendEmail(toEmail, subject, body);
        }
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
     * Send partner approval notification email (HTML)
     */
    public void sendPartnerApprovedEmail(String toEmail, String partnerName, String facilityName,
                                         String registrationNumber, String remarks) {
        log.info("Sending partner approval email to: {}", toEmail);

        String subject = "🎉 Partner Application Approved - ELocate";

        if (useHtmlTemplates) {
            try {
                Map<String, Object> variables = Map.of(
                    "partnerName", partnerName,
                    "facilityName", facilityName,
                    "registrationNumber", registrationNumber,
                    "remarks", remarks != null ? remarks : "",
                    "loginUrl", appBaseUrl + "/sign-in"
                );

                String htmlBody = templateService.processTemplate("partner-approved", variables);
                sendHtmlEmail(toEmail, subject, htmlBody);
                return;
            } catch (Exception e) {
                log.error("Failed to send HTML partner approval email, falling back to plain text", e);
            }
        }

        // Fallback to plain text
        StringBuilder body = new StringBuilder();
        body.append("Congratulations! Your partner application has been approved.\n\n");
        body.append("Partner Details:\n");
        body.append("- Name: ").append(partnerName).append("\n");
        body.append("- Facility: ").append(facilityName).append("\n");
        body.append("- Registration Number: ").append(registrationNumber).append("\n");
        body.append("- Status: APPROVED & ACTIVE\n\n");

        if (remarks != null && !remarks.isBlank()) {
            body.append("Admin Notes: ").append(remarks).append("\n\n");
        }

        body.append("Next Steps:\n");
        body.append("1. Login to your account using your registered email and password\n");
        body.append("2. Complete your facility profile\n");
        body.append("3. Start receiving recycle requests from citizens\n");
        body.append("4. Manage pickups and drivers through your dashboard\n\n");
        body.append("Login here: ").append(appBaseUrl).append("/sign-in\n\n");
        body.append("Welcome to the ELocate partner network!\n\n");
        body.append("Best regards,\n");
        body.append("ELocate Team");

        sendEmail(toEmail, subject, body.toString());
    }

    /**
     * Send partner rejection notification email (HTML)
     */
    public void sendPartnerRejectedEmail(String toEmail, String partnerName, String facilityName,
                                         String registrationNumber, String remarks) {
        log.info("Sending partner rejection email to: {}", toEmail);

        String subject = "Partner Application Update - ELocate";

        if (useHtmlTemplates) {
            try {
                Map<String, Object> variables = Map.of(
                    "partnerName", partnerName,
                    "facilityName", facilityName,
                    "registrationNumber", registrationNumber,
                    "remarks", remarks != null ? remarks : "",
                    "supportUrl", appBaseUrl + "/citizen/support"
                );

                String htmlBody = templateService.processTemplate("partner-rejected", variables);
                sendHtmlEmail(toEmail, subject, htmlBody);
                return;
            } catch (Exception e) {
                log.error("Failed to send HTML partner rejection email, falling back to plain text", e);
            }
        }

        // Fallback to plain text
        StringBuilder body = new StringBuilder();
        body.append("Thank you for your interest in becoming an ELocate partner.\n\n");
        body.append("After careful review, we regret to inform you that your application has not been approved at this time.\n\n");
        body.append("Application Details:\n");
        body.append("- Name: ").append(partnerName).append("\n");
        body.append("- Facility: ").append(facilityName).append("\n");
        body.append("- Registration Number: ").append(registrationNumber).append("\n");
        body.append("- Status: NOT APPROVED\n\n");

        if (remarks != null && !remarks.isBlank()) {
            body.append("Reason for Rejection:\n").append(remarks).append("\n\n");
        }

        body.append("What You Can Do:\n");
        body.append("- Review the feedback provided above\n");
        body.append("- Address any issues mentioned\n");
        body.append("- Reapply once you've met the requirements\n");
        body.append("- Contact support if you need clarification\n\n");
        body.append("Support: ").append(appBaseUrl).append("/citizen/support\n\n");
        body.append("We appreciate your interest in joining the ELocate network.\n\n");
        body.append("Best regards,\n");
        body.append("ELocate Team");

        sendEmail(toEmail, subject, body.toString());
    }


    /**
     * Send test email (for testing email configuration)
     *
     * @param toEmail Recipient email
     * @param subject Email subject
     * @param body Email body
     */
    public void sendTestEmail(String toEmail, String subject, String body) {
        log.info("Sending test email to: {}", toEmail);
        sendEmail(toEmail, subject, body);
    }


    /**
     * Send simple email
     */
    private void sendEmail(String to, String subject, String body) {
        // Use SendGrid if configured, otherwise fall back to SMTP
        if ("sendgrid".equalsIgnoreCase(emailProvider) && sendGridEmailService != null) {
            log.info("Using SendGrid HTTP API to send email");
            sendGridEmailService.sendEmail(to, subject, body);
            return;
        }

        // Fall back to SMTP
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

    /**
     * Send reminder email to intermediary/facility owner
     */
    public void sendReminderToIntermediaryEmail(String toEmail, String requestId, String deviceName,
                                                 String citizenName, String citizenEmail, String currentStatus,
                                                 String submittedDate, String comment, String dashboardUrl) {
        if (!isEmailEnabled() || !reminderEnabled) {
            log.info("Reminder email disabled, skipping");
            return;
        }

        log.info("Sending reminder email to intermediary: {}", toEmail);

        String subject = "⏰ Reminder: Pending Recycle Request #" + requestId;

        if (useHtmlTemplates) {
            try {
                Map<String, Object> variables = new HashMap<>();
                variables.put("requestId", requestId);
                variables.put("deviceName", deviceName);
                variables.put("citizenName", citizenName);
                variables.put("citizenEmail", citizenEmail);
                variables.put("currentStatus", currentStatus);
                variables.put("submittedDate", submittedDate);
                variables.put("comment", comment);
                variables.put("dashboardUrl", dashboardUrl);

                String htmlBody = templateService.processTemplate("citizen-reminder.html", variables);
                sendHtmlEmail(toEmail, subject, htmlBody);
                return;
            } catch (Exception e) {
                log.warn("Failed to send HTML reminder email, falling back to plain text: {}", e.getMessage());
            }
        }

        // Fallback to plain text
        StringBuilder body = new StringBuilder();
        body.append("REMINDER: Pending Recycle Request\n\n");
        body.append("A citizen has sent you a reminder about their pending recycle request.\n\n");
        body.append("Request Details:\n");
        body.append("- Request ID: ").append(requestId).append("\n");
        body.append("- Device: ").append(deviceName).append("\n");
        body.append("- Citizen: ").append(citizenName).append(" (").append(citizenEmail).append(")\n");
        body.append("- Current Status: ").append(currentStatus).append("\n");
        body.append("- Submitted On: ").append(submittedDate).append("\n\n");

        if (comment != null && !comment.isBlank()) {
            body.append("Message from Citizen:\n");
            body.append("\"").append(comment).append("\"\n\n");
        }

        body.append("Please review this request and take the necessary action.\n\n");
        body.append("View Request: ").append(dashboardUrl).append("\n\n");
        body.append("Best regards,\n");
        body.append("ELocate Team");

        sendEmail(toEmail, subject, body.toString());
    }

    /**
     * Send HTML email
     */
    private void sendHtmlEmail(String to, String subject, String htmlBody) {
        // Use SendGrid if configured
        if ("sendgrid".equalsIgnoreCase(emailProvider) && sendGridEmailService != null) {
            log.info("Using SendGrid HTTP API to send HTML email");
            sendGridEmailService.sendHtmlEmail(to, subject, htmlBody);
            return;
        }

        // Fall back to SMTP with HTML support
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true = HTML

            mailSender.send(message);
            log.info("HTML email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send HTML email to: {}, error: {}", to, e.getMessage());
            // Don't throw exception - operations should still succeed even if email fails
        }
    }
}
