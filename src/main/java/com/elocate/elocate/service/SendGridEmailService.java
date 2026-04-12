package com.elocate.elocate.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@Async
@ConditionalOnProperty(name = "app.email.provider", havingValue = "sendgrid")
public class SendGridEmailService {

    @Value("${sendgrid.api.key:}")
    private String apiKey;

    @Value("${app.email.from:noreply@elocate.com}")
    private String fromEmail;

    @Value("${app.email.from.name:ELocate}")
    private String fromName;

    private static final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";

    private final RestTemplate restTemplate = new RestTemplate();

    @PostConstruct
    public void debugConfig() {
        if (apiKey == null || apiKey.isEmpty()) {
            log.error("[Brevo] ❌ API key is EMPTY — check sendgrid.api.key / SENDGRID_API_KEY env var");
        } else {
            String masked = apiKey.substring(0, Math.min(12, apiKey.length()))
                    + "..." + apiKey.substring(Math.max(0, apiKey.length() - 6));
            log.info("[Brevo] ✅ API key loaded: {} (length={})", masked, apiKey.length());
        }
        log.info("[Brevo] fromEmail={}, fromName={}", fromEmail, fromName);
    }

    public void sendEmail(String toEmail, String subject, String body) {
        sendBrevoEmail(toEmail, subject, body, false);
    }

    public void sendHtmlEmail(String toEmail, String subject, String htmlBody) {
        sendBrevoEmail(toEmail, subject, htmlBody, true);
    }

    private void sendBrevoEmail(String toEmail, String subject, String content, boolean isHtml) {
        if (apiKey == null || apiKey.isEmpty()) {
            log.error("[Brevo] ❌ API key empty — skipping email to {}", toEmail);
            return;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", apiKey);

            Map<String, Object> body = Map.of(
                "sender",  Map.of("name", fromName, "email", fromEmail),
                "to",      List.of(Map.of("email", toEmail)),
                "subject", subject,
                isHtml ? "htmlContent" : "textContent", content
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(BREVO_API_URL, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("[Brevo] ✅ Email sent to {} — status={}", toEmail, response.getStatusCode());
            } else {
                log.error("[Brevo] ❌ Failed to send to {} — status={}, body={}", toEmail, response.getStatusCode(), response.getBody());
            }
        } catch (Exception e) {
            log.error("[Brevo] ❌ Exception sending to {}: {}", toEmail, e.getMessage());
        }
    }
}
