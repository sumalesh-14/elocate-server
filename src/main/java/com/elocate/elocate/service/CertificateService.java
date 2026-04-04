package com.elocate.elocate.service;

import com.elocate.elocate.model.RecycleRequest;
import com.elocate.elocate.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
@RequiredArgsConstructor
public class CertificateService {

    private final S3Service s3Service;

    @Value("${app.base.url:http://localhost:3000}")
    private String appBaseUrl;

    private static final String TEMPLATE_PATH     = "certificate-templates/recycling-certificate.html";
    private static final String PDF_TEMPLATE_PATH = "certificate-templates/recycling-certificate-pdf.html";

    /**
     * Load template, fill variables, upload to S3, return public URL.
     */
    public String generateAndUploadCertificate(RecycleRequest request, User citizen) {
        log.info("Generating certificate for request: {}", request.getId());

        String html = buildCertificateHtml(request, citizen);

        String key = "certificates/" + request.getId() + ".html";
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        s3Service.uploadFile(key, new ByteArrayInputStream(bytes), bytes.length, "text/html");

        String url = s3Service.getPublicUrl(key);
        log.info("Certificate uploaded to: {}", url);
        return url;
    }

    public String buildCertificateHtml(RecycleRequest request, User citizen) {
        return buildHtml(request, citizen, TEMPLATE_PATH);
    }

    /** Builds using the PDF-optimised template (table layout, no flexbox/gradients). */
    public String buildCertificatePdfHtml(RecycleRequest request, User citizen) {
        return buildHtml(request, citizen, PDF_TEMPLATE_PATH);
    }

    private String buildHtml(RecycleRequest request, User citizen, String templatePath) {
        String template = loadTemplate(templatePath);

        String citizenName   = citizen.getFullName() != null ? citizen.getFullName() : "Valued Citizen";
        String deviceName    = request.getDeviceModel() != null ? request.getDeviceModel().getModelName() : "Electronic Device";
        String requestNumber = request.getRequestNumber() != null ? request.getRequestNumber() : request.getId().toString().substring(0, 8).toUpperCase();
        String date          = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"));

        return template
                .replace("{{citizenName}}",    citizenName)
                .replace("{{deviceName}}",     deviceName)
                .replace("{{requestNumber}}",  requestNumber)
                .replace("{{requestId}}",      request.getId().toString())
                .replace("{{certificateDate}}", date)
                .replace("{{appBaseUrl}}",     appBaseUrl);
    }

    private String loadTemplate(String path) {
        try {
            ClassPathResource resource = new ClassPathResource(path);
            return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Failed to load certificate template: {}", e.getMessage());
            throw new RuntimeException("Certificate template not found: " + path);
        }
    }
}
