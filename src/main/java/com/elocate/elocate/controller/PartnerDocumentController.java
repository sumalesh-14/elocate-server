package com.elocate.elocate.controller;

import com.elocate.elocate.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.Map;
import java.util.UUID;

/**
 * Handles partner verification document uploads to S3.
 * Called before registration — returns the S3 URL which is then
 * included in the registration JSON payload.
 */
@RestController
@RequestMapping("/api/v1/partner-auth")
@RequiredArgsConstructor
@Slf4j
public class PartnerDocumentController {

    private final S3Service s3Service; // kept for potential future use

    @Value("${aws.access-key-id}")
    private String accessKeyId;

    @Value("${aws.secret-access-key}")
    private String secretAccessKey;

    @Value("${aws.s3.documents-bucket-name}")
    private String documentsBucket;

    @Value("${aws.s3.region}")
    private String region;

    private S3Client docsS3Client;

    @PostConstruct
    public void init() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
        this.docsS3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
        log.info("✅ Partner docs S3 client initialized — bucket: {}", documentsBucket);
    }

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB
    private static final java.util.Set<String> ALLOWED_TYPES = java.util.Set.of(
            "application/pdf", "image/jpeg", "image/jpg", "image/png"
    );

    @PostMapping(value = "/upload-document", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadDocument(@RequestParam("file") MultipartFile file) {
        log.info("Partner document upload — filename: {}, size: {} bytes, type: {}",
                file.getOriginalFilename(), file.getSize(), file.getContentType());

        if (file.isEmpty() || file.getSize() > MAX_FILE_SIZE) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "File must be non-empty and under 10 MB"));
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Only PDF, JPG, or PNG files are allowed"));
        }

        try {
            String ext = getExtension(file.getOriginalFilename(), contentType);
            String key = String.format("partner-documents/%s/%s.%s",
                    UUID.randomUUID(), UUID.randomUUID(), ext);

            InputStream inputStream = file.getInputStream();
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(documentsBucket)
                    .key(key)
                    .contentType(contentType)
                    .build();

            docsS3Client.putObject(putRequest, RequestBody.fromInputStream(inputStream, file.getSize()));

            String url = String.format("https://%s.s3.%s.amazonaws.com/%s", documentsBucket, region, key);
            log.info("✅ Partner document uploaded: {}", url);
            return ResponseEntity.ok(Map.of("documentUrl", url));

        } catch (Exception e) {
            log.error("Failed to upload partner document", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Upload failed: " + e.getMessage()));
        }
    }

    private String getExtension(String filename, String contentType) {
        if (filename != null && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        }
        return switch (contentType.toLowerCase()) {
            case "application/pdf" -> "pdf";
            case "image/png" -> "png";
            default -> "jpg";
        };
    }
}
