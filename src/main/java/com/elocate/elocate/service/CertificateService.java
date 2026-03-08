package com.elocate.elocate.service;

import com.elocate.elocate.model.RecycleRequest;
import com.elocate.elocate.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Service for generating recycling certificates
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CertificateService {

    private final S3Service s3Service;

    @Value("${app.base.url:http://localhost:3000}")
    private String appBaseUrl;

    /**
     * Generate certificate HTML and upload to S3
     */
    public String generateAndUploadCertificate(RecycleRequest request, User citizen) {
        log.info("Generating certificate for request: {}", request.getId());

        String certificateHtml = generateCertificateHtml(request, citizen);
        
        // Upload to S3
        String certificateKey = "certificates/" + request.getId() + ".html";
        byte[] htmlBytes = certificateHtml.getBytes();
        InputStream inputStream = new ByteArrayInputStream(htmlBytes);
        
        s3Service.uploadFile(certificateKey, inputStream, htmlBytes.length, "text/html");
        
        String certificateUrl = s3Service.getPublicUrl(certificateKey);
        log.info("Certificate uploaded to: {}", certificateUrl);
        
        return certificateUrl;
    }

    /**
     * Generate certificate HTML content
     */
    private String generateCertificateHtml(RecycleRequest request, User citizen) {
        String citizenName = citizen.getFullName() != null ? citizen.getFullName() : "Valued Citizen";
        String deviceName = request.getDeviceModel() != null ? 
            request.getDeviceModel().getModelName() : "Electronic Device";
        String certificateDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"));
        String requestId = request.getId().toString().substring(0, 8).toUpperCase();

        // Build HTML without using String.format to avoid %% escaping issues
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"en\">\n");
        html.append("<head>\n");
        html.append("    <meta charset=\"UTF-8\">\n");
        html.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("    <title>ELocate Recycling Certificate</title>\n");
        html.append("    <style>\n");
        html.append("        * {\n");
        html.append("            margin: 0;\n");
        html.append("            padding: 0;\n");
        html.append("            box-sizing: border-box;\n");
        html.append("        }\n");
        html.append("        \n");
        html.append("        body {\n");
        html.append("            font-family: 'Georgia', serif;\n");
        html.append("            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);\n");
        html.append("            min-height: 100vh;\n");
        html.append("            display: flex;\n");
        html.append("            justify-content: center;\n");
        html.append("            align-items: center;\n");
        html.append("            padding: 20px;\n");
        html.append("        }\n");
        html.append("        \n");
        html.append("        .certificate {\n");
        html.append("            background: white;\n");
        html.append("            max-width: 900px;\n");
        html.append("            width: 100%;\n");
        html.append("            padding: 60px;\n");
        html.append("            border-radius: 20px;\n");
        html.append("            box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);\n");
        html.append("            position: relative;\n");
        html.append("            overflow: hidden;\n");
        html.append("        }\n");
        html.append("        \n");
        html.append("        .certificate::before {\n");
        html.append("            content: '';\n");
        html.append("            position: absolute;\n");
        html.append("            top: 0;\n");
        html.append("            left: 0;\n");
        html.append("            right: 0;\n");
        html.append("            height: 10px;\n");
        html.append("            background: linear-gradient(90deg, #22c55e, #10b981, #059669);\n");
        html.append("        }\n");
        html.append("        \n");
        html.append("        .certificate::after {\n");
        html.append("            content: '';\n");
        html.append("            position: absolute;\n");
        html.append("            bottom: 0;\n");
        html.append("            left: 0;\n");
        html.append("            right: 0;\n");
        html.append("            height: 10px;\n");
        html.append("            background: linear-gradient(90deg, #22c55e, #10b981, #059669);\n");
        html.append("        }\n");
        html.append("        \n");
        html.append("        .header {\n");
        html.append("            text-align: center;\n");
        html.append("            margin-bottom: 40px;\n");
        html.append("        }\n");
        html.append("        \n");
        html.append("        .logo {\n");
        html.append("            font-size: 48px;\n");
        html.append("            color: #10b981;\n");
        html.append("            margin-bottom: 10px;\n");
        html.append("        }\n");
        html.append("        \n");
        html.append("        .title {\n");
        html.append("            font-size: 42px;\n");
        html.append("            color: #1f2937;\n");
        html.append("            font-weight: bold;\n");
        html.append("            margin-bottom: 10px;\n");
        html.append("            letter-spacing: 2px;\n");
        html.append("        }\n");
        html.append("        \n");
        html.append("        .subtitle {\n");
        html.append("            font-size: 18px;\n");
        html.append("            color: #6b7280;\n");
        html.append("            font-style: italic;\n");
        html.append("        }\n");
        html.append("        \n");
        html.append("        .content {\n");
        html.append("            text-align: center;\n");
        html.append("            margin: 40px 0;\n");
        html.append("            line-height: 2;\n");
        html.append("        }\n");
        html.append("        \n");
        html.append("        .awarded-to {\n");
        html.append("            font-size: 20px;\n");
        html.append("            color: #6b7280;\n");
        html.append("            margin-bottom: 15px;\n");
        html.append("        }\n");
        html.append("        \n");
        html.append("        .recipient-name {\n");
        html.append("            font-size: 36px;\n");
        html.append("            color: #1f2937;\n");
        html.append("            font-weight: bold;\n");
        html.append("            margin: 20px 0;\n");
        html.append("            padding: 10px 0;\n");
        html.append("            border-bottom: 3px solid #10b981;\n");
        html.append("            display: inline-block;\n");
        html.append("        }\n");
        html.append("        \n");
        html.append("        .message {\n");
        html.append("            font-size: 18px;\n");
        html.append("            color: #4b5563;\n");
        html.append("            margin: 30px auto;\n");
        html.append("            max-width: 700px;\n");
        html.append("            line-height: 1.8;\n");
        html.append("        }\n");
        html.append("        \n");
        html.append("        .highlight {\n");
        html.append("            color: #10b981;\n");
        html.append("            font-weight: bold;\n");
        html.append("        }\n");
        html.append("        \n");
        html.append("        .device-info {\n");
        html.append("            background: #f3f4f6;\n");
        html.append("            padding: 20px;\n");
        html.append("            border-radius: 10px;\n");
        html.append("            margin: 30px 0;\n");
        html.append("            border-left: 5px solid #10b981;\n");
        html.append("        }\n");
        html.append("        \n");
        html.append("        .device-info p {\n");
        html.append("            font-size: 16px;\n");
        html.append("            color: #374151;\n");
        html.append("            margin: 8px 0;\n");
        html.append("        }\n");
        html.append("        \n");
        html.append("        .footer {\n");
        html.append("            margin-top: 50px;\n");
        html.append("            display: flex;\n");
        html.append("            justify-content: space-between;\n");
        html.append("            align-items: flex-end;\n");
        html.append("            padding-top: 30px;\n");
        html.append("            border-top: 2px solid #e5e7eb;\n");
        html.append("        }\n");
        html.append("        \n");
        html.append("        .signature {\n");
        html.append("            text-align: center;\n");
        html.append("        }\n");
        html.append("        \n");
        html.append("        .signature-line {\n");
        html.append("            width: 200px;\n");
        html.append("            border-top: 2px solid #1f2937;\n");
        html.append("            margin: 10px auto;\n");
        html.append("        }\n");
        html.append("        \n");
        html.append("        .signature-text {\n");
        html.append("            font-size: 14px;\n");
        html.append("            color: #6b7280;\n");
        html.append("            margin-top: 5px;\n");
        html.append("        }\n");
        html.append("        \n");
        html.append("        .date {\n");
        html.append("            text-align: center;\n");
        html.append("        }\n");
        html.append("        \n");
        html.append("        .date-value {\n");
        html.append("            font-size: 16px;\n");
        html.append("            color: #1f2937;\n");
        html.append("            font-weight: bold;\n");
        html.append("        }\n");
        html.append("        \n");
        html.append("        .date-label {\n");
        html.append("            font-size: 14px;\n");
        html.append("            color: #6b7280;\n");
        html.append("            margin-top: 5px;\n");
        html.append("        }\n");
        html.append("        \n");
        html.append("        .certificate-id {\n");
        html.append("            text-align: center;\n");
        html.append("            margin-top: 30px;\n");
        html.append("            font-size: 12px;\n");
        html.append("            color: #9ca3af;\n");
        html.append("        }\n");
        html.append("        \n");
        html.append("        .badge {\n");
        html.append("            position: absolute;\n");
        html.append("            top: 40px;\n");
        html.append("            right: 40px;\n");
        html.append("            width: 100px;\n");
        html.append("            height: 100px;\n");
        html.append("            background: linear-gradient(135deg, #10b981, #059669);\n");
        html.append("            border-radius: 50%;\n");
        html.append("            display: flex;\n");
        html.append("            flex-direction: column;\n");
        html.append("            justify-content: center;\n");
        html.append("            align-items: center;\n");
        html.append("            color: white;\n");
        html.append("            box-shadow: 0 4px 15px rgba(16, 185, 129, 0.4);\n");
        html.append("        }\n");
        html.append("        \n");
        html.append("        .badge-icon {\n");
        html.append("            font-size: 32px;\n");
        html.append("            margin-bottom: 5px;\n");
        html.append("        }\n");
        html.append("        \n");
        html.append("        .badge-text {\n");
        html.append("            font-size: 11px;\n");
        html.append("            font-weight: bold;\n");
        html.append("            text-transform: uppercase;\n");
        html.append("        }\n");
        html.append("        \n");
        html.append("        @media print {\n");
        html.append("            body {\n");
        html.append("                background: white;\n");
        html.append("            }\n");
        html.append("            \n");
        html.append("            .certificate {\n");
        html.append("                box-shadow: none;\n");
        html.append("                max-width: 100%;\n");
        html.append("            }\n");
        html.append("        }\n");
        html.append("        \n");
        html.append("        @media (max-width: 768px) {\n");
        html.append("            .certificate {\n");
        html.append("                padding: 30px 20px;\n");
        html.append("            }\n");
        html.append("            \n");
        html.append("            .title {\n");
        html.append("                font-size: 32px;\n");
        html.append("            }\n");
        html.append("            \n");
        html.append("            .recipient-name {\n");
        html.append("                font-size: 28px;\n");
        html.append("            }\n");
        html.append("            \n");
        html.append("            .badge {\n");
        html.append("                width: 80px;\n");
        html.append("                height: 80px;\n");
        html.append("                top: 20px;\n");
        html.append("                right: 20px;\n");
        html.append("            }\n");
        html.append("            \n");
        html.append("            .badge-icon {\n");
        html.append("                font-size: 24px;\n");
        html.append("            }\n");
        html.append("            \n");
        html.append("            .footer {\n");
        html.append("                flex-direction: column;\n");
        html.append("                gap: 30px;\n");
        html.append("            }\n");
        html.append("        }\n");
        html.append("    </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("    <div class=\"certificate\">\n");
        html.append("        <div class=\"badge\">\n");
        html.append("            <div class=\"badge-icon\">♻️</div>\n");
        html.append("            <div class=\"badge-text\">Verified</div>\n");
        html.append("        </div>\n");
        html.append("        \n");
        html.append("        <div class=\"header\">\n");
        html.append("            <div class=\"logo\">🌍 ELocate</div>\n");
        html.append("            <h1 class=\"title\">CERTIFICATE OF APPRECIATION</h1>\n");
        html.append("            <p class=\"subtitle\">For Environmental Stewardship</p>\n");
        html.append("        </div>\n");
        html.append("        \n");
        html.append("        <div class=\"content\">\n");
        html.append("            <p class=\"awarded-to\">This certificate is proudly awarded to</p>\n");
        html.append("            \n");
        html.append("            <div class=\"recipient-name\">").append(citizenName).append("</div>\n");
        html.append("            \n");
        html.append("            <p class=\"message\">\n");
        html.append("                For your <span class=\"highlight\">fantastic contribution</span> to environmental sustainability \n");
        html.append("                by responsibly recycling electronic waste through ELocate. Your commitment to protecting \n");
        html.append("                our planet and reducing e-waste pollution is truly commendable and makes a real difference \n");
        html.append("                in creating a <span class=\"highlight\">greener future</span> for generations to come.\n");
        html.append("            </p>\n");
        html.append("            \n");
        html.append("            <div class=\"device-info\">\n");
        html.append("                <p><strong>Device Recycled:</strong> ").append(deviceName).append("</p>\n");
        html.append("                <p><strong>Certificate ID:</strong> #").append(requestId).append("</p>\n");
        html.append("                <p><strong>Impact:</strong> You've helped prevent harmful materials from polluting our environment!</p>\n");
        html.append("            </div>\n");
        html.append("            \n");
        html.append("            <p class=\"message\">\n");
        html.append("                Thank you for being an <span class=\"highlight\">environmental champion</span> and inspiring \n");
        html.append("                others to take action. Together, we're building a sustainable tomorrow! 🌱\n");
        html.append("            </p>\n");
        html.append("        </div>\n");
        html.append("        \n");
        html.append("        <div class=\"footer\">\n");
        html.append("            <div class=\"signature\">\n");
        html.append("                <div class=\"signature-line\"></div>\n");
        html.append("                <p class=\"signature-text\">ELocate Team</p>\n");
        html.append("                <p class=\"signature-text\" style=\"font-size: 12px;\">Environmental Services</p>\n");
        html.append("            </div>\n");
        html.append("            \n");
        html.append("            <div class=\"date\">\n");
        html.append("                <p class=\"date-value\">").append(certificateDate).append("</p>\n");
        html.append("                <p class=\"date-label\">Certificate Date</p>\n");
        html.append("            </div>\n");
        html.append("        </div>\n");
        html.append("        \n");
        html.append("        <div class=\"certificate-id\">\n");
        html.append("            <p>This certificate can be verified at ").append(appBaseUrl).append("/verify/").append(request.getId()).append("</p>\n");
        html.append("        </div>\n");
        html.append("    </div>\n");
        html.append("</body>\n");
        html.append("</html>");
        
        return html.toString();
    }
}
