package com.datn.backend.service.impl;

import com.datn.backend.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Override
    public String uploadFile(MultipartFile file, String folderPath) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String fileName = UUID.randomUUID().toString() + extension;
        String objectKey = (folderPath != null && !folderPath.isEmpty()) ? 
                (folderPath.endsWith("/") ? folderPath + fileName : folderPath + "/" + fileName) : 
                fileName;

        if (objectKey.startsWith("/")) {
            objectKey = objectKey.substring(1);
        }

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            URL reportUrl = s3Client.utilities().getUrl(GetUrlRequest.builder().bucket(bucketName).key(objectKey).build());
            return reportUrl.toString();

        } catch (IOException e) {
            log.error("Failed to upload file to S3", e);
            throw new RuntimeException("Failed to upload file to S3", e);
        }
    }

    @Override
    public String uploadFileWithKey(MultipartFile file, String objectKey) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        // Remove leading slash if present
        String key = objectKey.startsWith("/") ? objectKey.substring(1) : objectKey;

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            URL reportUrl = s3Client.utilities().getUrl(GetUrlRequest.builder().bucket(bucketName).key(key).build());
            return reportUrl.toString();

        } catch (IOException e) {
            log.error("Failed to upload file to S3 with key: {}", key, e);
            throw new RuntimeException("Failed to upload file to S3", e);
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }

        try {
            URL url = new URL(fileUrl);
            String path = url.getPath();
            
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            
            String objectKey = path;
            
            if (objectKey.startsWith(bucketName + "/")) {
                objectKey = objectKey.substring(bucketName.length() + 1);
            }

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("Successfully deleted file from S3: {}", objectKey);

        } catch (Exception e) {
            log.error("Failed to delete file from S3: {}", fileUrl, e);
            throw new RuntimeException("Failed to delete file from S3", e);
        }
    }

    @Override
    public void moveFolderS3(String oldPrefix, String newPrefix) {
        if (oldPrefix == null || oldPrefix.isEmpty() || newPrefix == null || newPrefix.isEmpty()) {
            return;
        }

        try {
            boolean isTruncated = true;
            String continuationToken = null;

            while (isTruncated) {
                ListObjectsV2Request.Builder listReqBuilder = ListObjectsV2Request.builder()
                        .bucket(bucketName)
                        .prefix(oldPrefix);
                
                if (continuationToken != null) {
                    listReqBuilder.continuationToken(continuationToken);
                }

                ListObjectsV2Response listRes = s3Client.listObjectsV2(listReqBuilder.build());

                for (S3Object s3Object : listRes.contents()) {
                    String oldKey = s3Object.key();
                    String newKey = newPrefix + oldKey.substring(oldPrefix.length());

                    CopyObjectRequest copyReq = CopyObjectRequest.builder()
                            .sourceBucket(bucketName)
                            .sourceKey(oldKey)
                            .destinationBucket(bucketName)
                            .destinationKey(newKey)
                            .build();
                    s3Client.copyObject(copyReq);

                    DeleteObjectRequest deleteReq = DeleteObjectRequest.builder()
                            .bucket(bucketName)
                            .key(oldKey)
                            .build();
                    s3Client.deleteObject(deleteReq);
                }

                isTruncated = listRes.isTruncated();
                continuationToken = listRes.nextContinuationToken();
            }
            log.info("Successfully moved S3 folder from {} to {}", oldPrefix, newPrefix);
        } catch (Exception e) {
            log.error("Failed to move S3 folder from {} to {}", oldPrefix, newPrefix, e);
            throw new RuntimeException("Failed to move S3 folder", e);
        }
    }

    @Override
    public void deleteFolderS3(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return;
        }

        try {
            boolean isTruncated = true;
            String continuationToken = null;

            while (isTruncated) {
                ListObjectsV2Request.Builder listReqBuilder = ListObjectsV2Request.builder()
                        .bucket(bucketName)
                        .prefix(prefix);
                
                if (continuationToken != null) {
                    listReqBuilder.continuationToken(continuationToken);
                }

                ListObjectsV2Response listRes = s3Client.listObjectsV2(listReqBuilder.build());

                for (S3Object s3Object : listRes.contents()) {
                    DeleteObjectRequest deleteReq = DeleteObjectRequest.builder()
                            .bucket(bucketName)
                            .key(s3Object.key())
                            .build();
                    s3Client.deleteObject(deleteReq);
                }

                isTruncated = listRes.isTruncated();
                continuationToken = listRes.nextContinuationToken();
            }
            log.info("Successfully deleted S3 folder with prefix: {}", prefix);
        } catch (Exception e) {
            log.error("Failed to delete S3 folder with prefix: {}", prefix, e);
            throw new RuntimeException("Failed to delete S3 folder", e);
        }
    }
}
