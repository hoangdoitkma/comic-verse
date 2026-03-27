package com.datn.backend.service;

import org.springframework.web.multipart.MultipartFile;

public interface S3Service {
    String uploadFile(MultipartFile file, String folderPath);
    String uploadFileWithKey(MultipartFile file, String objectKey);
    void deleteFile(String fileUrl);
    void moveFolderS3(String oldPrefix, String newPrefix);
    void deleteFolderS3(String prefix);
}

