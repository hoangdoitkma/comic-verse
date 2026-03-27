package com.datn.backend;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.core.sync.RequestBody;

public class TestS3 {
    public static void main(String[] args) {
        String accessKey = "AKIA6F6ONXPVDOTS275Z";
        String secretKey = "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";
        String region = "ap-southeast-1";
        String bucketName = "comicverse-storage";
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
}
