package com.elocate.elocate.controller;

import com.elocate.elocate.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/test")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Email Test", description = "Test email functionality (Public endpoint)")
public class EmailTestController {

    private final EmailService emailService;

    private static final String TEST_EMAIL = "sumaleshka@gmail.com";

    @PostMapping("/send-email")
    @Operation(summary = "Send test email", description = "Send a test email to verify email configuration")
    public ResponseEntity<?> sendTestEmail(HttpServletRequest request) {
        String clientIp = getClientIp(request);
        log.info("Test email request from IP: {}", clientIp);

        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            
            String subject = "ELocate Email Test - " + timestamp;
            String body = buildTestEmailBody(clientIp, timestamp);

            emailService.sendTestEmail(TEST_EMAIL, subject, body);

            log.info("Test email sent successfully to: {}", TEST_EMAIL);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Test email sent successfully",
                    "recipient", TEST_EMAIL,
                    "timestamp", timestamp,
                    "clientIp", clientIp
            ));

        } catch (Exception e) {
            log.error("Failed to send test email", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "error", "Failed to send test email",
                            "message", e.getMessage()
                    ));
        }
    }

    @GetMapping("/email-status")
    @Operation(summary = "Check email configuration", description = "Check if email service is configured")
    public ResponseEntity<?> checkEmailStatus(HttpServletRequest request) {
        String clientIp = getClientIp(request);
        log.info("Email status check from IP: {}", clientIp);

        Map<String, Object> status = new HashMap<>();
        status.put("emailConfigured", true);
        status.put("testRecipient", TEST_EMAIL);
        status.put("clientIp", clientIp);
        status.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        status.put("note", "This endpoint is publicly accessible via Spring Security configuration");

        return ResponseEntity.ok(status);
    }

    private String buildTestEmailBody(String clientIp, String timestamp) {
        return String.format("""
                ELocate Email Configuration Test
                ================================
                
                This is a test email to verify that the email service is working correctly.
                
                Test Details:
                - Timestamp: %s
                - Request IP: %s
                - Recipient: %s
                
                Email Configuration:
                - SMTP Host: Configured
                - SMTP Port: 587 (STARTTLS)
                - Authentication: Enabled
                
                If you received this email, the email service is working correctly!
                
                ---
                ELocate E-Waste Management System
                """, timestamp, clientIp, TEST_EMAIL);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
