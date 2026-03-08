package com.elocate.elocate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.time.Duration;
import java.util.UUID;

/**
 * Service for AWS S3 operations
 */
@Service
@Slf4j
public class S3Service {

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.region}")
    private String region;

    @Value("${aws.access-key-id}")
    private String accessKeyId;

    @Value("${aws.secret-access-key}")
    private String secretAccessKey;

    private S3Client s3Client;
    private S3Presigner s3Presigner;

    @PostConstruct
    public void init() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
        
        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();

        this.s3Presigner = S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();

        log.info("✅ S3 Service initialized - Bucket: {}, Region: {}", bucketName, region);
    }

    /**
     * Generate pre-signed URL for direct upload from frontend
     */
    public String generatePresignedUploadUrl(String key, String contentType, int expirationMinutes) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(contentType)
                    .build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(expirationMinutes))
                    .putObjectRequest(putObjectRequest)
                    .build();

            PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
            String url = presignedRequest.url().toString();
            
            log.info("Generated pre-signed upload URL for key: {} with content-type: {}", key, contentType);
            return url;
        } catch (Exception e) {
            log.error("Failed to generate pre-signed URL for key: {}", key, e);
            throw new RuntimeException("Failed to generate upload URL", e);
        }
    }

    /**
     * Upload file directly to S3
     */
    public String uploadFile(String key, InputStream inputStream, long contentLength, String contentType) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(contentType)
                    // No ACL - using bucket policy for public access instead
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, contentLength));
            
            String url = getPublicUrl(key);
            log.info("✅ File uploaded successfully to S3: {}", key);
            return url;
        } catch (Exception e) {
            log.error("❌ Failed to upload file to S3: {}", key, e);
            throw new RuntimeException("Failed to upload file to S3", e);
        }
    }

    /**
     * Delete file from S3
     */
    public void deleteFile(String key) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("✅ File deleted from S3: {}", key);
        } catch (Exception e) {
            log.error("❌ Failed to delete file from S3: {}", key, e);
            throw new RuntimeException("Failed to delete file from S3", e);
        }
    }

    /**
     * Get public URL for a file
     */
    public String getPublicUrl(String key) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, key);
    }

    /**
     * Generate unique key for pickup photo
     */
    public String generatePickupPhotoKey(UUID requestId) {
        String filename = UUID.randomUUID().toString() + ".jpg";
        return String.format("pickups/%s/%s", requestId, filename);
    }
}
