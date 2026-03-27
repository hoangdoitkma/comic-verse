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
public class ChapterItemDTO {
    private Integer id;
    private BigDecimal chapterNumber;
    private String title;
    private String accessType;
    private Integer viewCount;
    private LocalDateTime createdAt;
}
