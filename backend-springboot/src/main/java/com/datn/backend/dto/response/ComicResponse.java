package com.datn.backend.dto.response;

import lombok.Data;

@Data
public class ComicResponse {
    private Integer id;
    private String title;
    private String synopsis;
    private String thumbnailUrl;
    private String contentType;
    private String comicFormat;
    private String status;
    private Integer totalChapters;
    private Integer viewCount;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;
}
