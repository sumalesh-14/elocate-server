package com.elocate.elocate.service;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * SendGrid HTTP API Email Service
 * Uses SendGrid's HTTP API instead of SMTP (works on Railway and other platforms that block SMTP)
 */
@Service
@Slf4j
@Async
@ConditionalOnProperty(name = "app.email.provider", havingValue = "sendgrid")
public class SendGridEmailService {

    @Value("${sendgrid.api.key:}")
    private String sendGridApiKey;

    @Value("${app.email.from:noreply@elocate.com}")
    private String fromEmail;

    @Value("${app.email.from.name:ELocate}")
    private String fromName;

    /**
     * Send email using SendGrid HTTP API
     */
    public void sendEmail(String toEmail, String subject, String body) {
        if (sendGridApiKey == null || sendGridApiKey.isEmpty()) {
            log.error("SendGrid API key not configured");
            return;
        }

        try {
            Email from = new Email(fromEmail, fromName);
            Email to = new Email(toEmail);
            Content content = new Content("text/plain", body);
            Mail mail = new Mail(from, subject, to, content);

            SendGrid sg = new SendGrid(sendGridApiKey);
            Request request = new Request();
            
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            
            Response response = sg.api(request);
            
            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                log.info("✅ Email sent successfully via SendGrid to: {}", toEmail);
            } else {
                log.error("❌ SendGrid API error: Status {}, Body: {}", 
                    response.getStatusCode(), response.getBody());
            }
            
        } catch (IOException e) {
            log.error("❌ Failed to send email via SendGrid to: {}, error: {}", toEmail, e.getMessage());
        }
    }

    /**
     * Send HTML email using SendGrid HTTP API
     */
    public void sendHtmlEmail(String toEmail, String subject, String htmlBody) {
        if (sendGridApiKey == null || sendGridApiKey.isEmpty()) {
            log.error("SendGrid API key not configured");
            return;
        }

        try {
            Email from = new Email(fromEmail, fromName);
            Email to = new Email(toEmail);
            Content content = new Content("text/html", htmlBody);
            Mail mail = new Mail(from, subject, to, content);

            SendGrid sg = new SendGrid(sendGridApiKey);
            Request request = new Request();
            
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            
            Response response = sg.api(request);
            
            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                log.info("✅ HTML email sent successfully via SendGrid to: {}", toEmail);
            } else {
                log.error("❌ SendGrid API error: Status {}, Body: {}", 
                    response.getStatusCode(), response.getBody());
            }
            
        } catch (IOException e) {
            log.error("❌ Failed to send HTML email via SendGrid to: {}, error: {}", toEmail, e.getMessage());
        }
    }
}
