package com.datn.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ChapterRequest {
    @NotNull(message = "Chapter number is required")
    private java.math.BigDecimal chapterNumber;

    @NotBlank(message = "Title cannot be empty")
    private String title;

    @NotNull(message = "Access type is required")
    private com.datn.backend.entity.enums.AccessType accessType;

    // For novel content; optional for comic pages
    private String content;

    // For 2-step comic upload: ordered list of file names (used by init-single endpoint)
    private List<String> pageFileNames;
}
