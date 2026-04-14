package com.elocate.elocate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Calls the elocate-whatsapp-bot service to send proactive WhatsApp notifications.
 * Fires-and-forgets — never blocks the main request flow.
 */
@Service
@Slf4j
public class WhatsAppNotificationService {

    private final WebClient webClient;
    private final String internalSecret;
    private final boolean enabled;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    public WhatsAppNotificationService(
            @Value("${whatsapp.bot.url:http://localhost:8081}") String botUrl,
            @Value("${whatsapp.bot.internal-secret:elocate-internal-secret}") String internalSecret,
            @Value("${whatsapp.bot.enabled:false}") boolean enabled) {

        this.internalSecret = internalSecret;
        this.enabled = enabled;
        this.webClient = WebClient.builder()
                .baseUrl(botUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /**
     * Send a "pickup assigned" notification to the driver via WhatsApp.
     * Called from RecycleRequestService.assignDriver().
     */
    public void notifyPickupAssigned(
            String driverPhone,
            String driverName,
            String requestNumber,
            String deviceName,
            String pickupAddress,
            LocalDate pickupDate,
            BigDecimal estimatedAmount,
            String comments) {

        if (!enabled) {
            log.debug("WhatsApp notifications disabled — skipping for request {}", requestNumber);
            return;
        }

        if (driverPhone == null || driverPhone.isBlank()) {
            log.warn("Driver phone is blank — cannot send WhatsApp notification for {}", requestNumber);
            return;
        }

        Map<String, String> payload = new HashMap<>();
        payload.put("driverPhone",    normalizePhone(driverPhone));
        payload.put("driverName",     driverName);
        payload.put("requestNumber",  requestNumber);
        payload.put("deviceName",     deviceName != null ? deviceName : "Device");
        payload.put("pickupAddress",  pickupAddress != null ? pickupAddress : "Address not set");
        payload.put("pickupDate",     pickupDate != null ? pickupDate.format(DATE_FMT) : "TBD");
        payload.put("estimatedAmount", estimatedAmount != null ? "₹" + estimatedAmount.toPlainString() : "TBD");
        payload.put("comments",       comments != null ? comments : "");

        webClient.post()
                .uri("/internal/notify/pickup-assigned")
                .header("X-Internal-Secret", internalSecret)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(r -> log.info("WhatsApp pickup-assigned notification sent for {}", requestNumber))
                .doOnError(e -> log.warn("WhatsApp notification failed for {} — {}", requestNumber, e.getMessage()))
                .subscribe(); // non-blocking fire-and-forget
    }

    /** Normalize phone: strip leading 0, add country code 91 if needed */
    private String normalizePhone(String phone) {
        phone = phone.replaceAll("[^0-9]", "");
        if (phone.startsWith("0")) phone = phone.substring(1);
        if (!phone.startsWith("91") && phone.length() == 10) phone = "91" + phone;
        return phone;
    }
}