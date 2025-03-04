package com.example.pfe_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {
    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.region}")
    private String region;

    public String uploadFile(MultipartFile file) {
        try {
            String objectKey = generateUniqueFileName(file.getOriginalFilename());
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            return objectKey; // Return only the object key instead of full URL
        } catch (IOException e) {
            log.error("Error uploading file to S3", e);
            throw new RuntimeException("Failed to upload file to S3", e);
        }
    }

    public String generatePresignedUrl(String objectKey) {
        try (S3Presigner presigner = S3Presigner.builder()
                .region(software.amazon.awssdk.regions.Region.of(region))
                .build()) {

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(60)) // URL expires in 60 minutes
                    .getObjectRequest(b -> b.bucket(bucketName).key(objectKey))
                    .build();

            PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);
            return presignedRequest.url().toString();
        }
    }
    
    public void deleteFile(String objectKey) {
        try {
            log.info("Deleting file from S3 with key: {}", objectKey);
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();
                    
            s3Client.deleteObject(deleteObjectRequest);
            log.info("Successfully deleted file from S3 with key: {}", objectKey);
        } catch (Exception e) {
            log.error("Error deleting file from S3 with key: {}", objectKey, e);
            throw new RuntimeException("Failed to delete file from S3", e);
        }
    }

    private String generateUniqueFileName(String originalFileName) {
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        return UUID.randomUUID().toString() + extension;
    }
} 