package com.datn.backend.dto.public_api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComicDTO {
    private Integer id;
    private String title;
    private String slug;
    private String thumbnailUrl;
    private Integer viewCount;
    private BigDecimal averageRating;
    private Integer totalChapters;
    private String status;
    private String contentType;
    private String comicFormat;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
