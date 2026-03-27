package com.datn.backend.dto.request;

import com.datn.backend.entity.enums.AccessType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class BulkChapterUploadRequest {

    @NotNull(message = "Chapters list is required")
    private List<ChapterFolder> chapters;

    @Data
    public static class ChapterFolder {
        @NotBlank(message = "Folder name is required")
        private String folderName; // sanitized slug, e.g. "chap-1"

        private String title; // optional chapter title

        @NotNull(message = "Access type is required")
        private AccessType accessType;

        // Ordered list of file names (alpha-numeric sorted by frontend)
        // Index in this list = page_number (1-based)
        @NotNull(message = "Page file names are required")
        private List<String> pageFileNames;
    }
}
