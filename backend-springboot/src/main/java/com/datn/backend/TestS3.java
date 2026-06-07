package com.datn.backend;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.core.sync.RequestBody;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class TestS3 {
    public static void main(String[] args) {
        String accessKey = requiredSetting("AWS_ACCESS_KEY_ID");
        String secretKey = requiredSetting("AWS_SECRET_ACCESS_KEY");
        String region = requiredSetting("AWS_REGION");
        String bucketName = requiredSetting("AWS_S3_BUCKET");
        String objectKey = "test-upload.txt";

        try {
            AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
            S3Client s3Client = S3Client.builder()
                    .region(Region.of(region))
                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .build();

            System.out.println("Attempting to upload to bucket: " + bucketName);
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .contentType("text/plain")
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromString("Hello S3"));
            System.out.println("Upload successful!");
        } catch (Exception e) {
            System.err.println("Upload failed. Error Details:");
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String requiredSetting(String key) {
        String value = System.getenv(key);
        if (value != null && !value.isBlank()) {
            return value;
        }

        for (Path envPath : List.of(Path.of(".env"), Path.of("..", ".env"))) {
            value = readDotEnvValue(envPath, key);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }

        throw new IllegalStateException("Missing required setting: " + key);
    }

    private static String readDotEnvValue(Path envPath, String key) {
        if (!Files.isRegularFile(envPath)) {
            return null;
        }

        try {
            for (String line : Files.readAllLines(envPath)) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }

                int separatorIndex = trimmed.indexOf('=');
                if (separatorIndex <= 0) {
                    continue;
                }

                String envKey = trimmed.substring(0, separatorIndex).trim();
                if (key.equals(envKey)) {
                    return trimmed.substring(separatorIndex + 1).trim();
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read " + envPath, e);
        }

        return null;
    }
}
