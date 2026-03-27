package com.datn.backend.dto.public_api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChapterDetailDTO {
    private Integer id;
    private BigDecimal chapterNumber;
    private String title;
    private List<String> pages; // URLs for comic, or paragraphs for novel
    private String content; // For novel
    private Integer nextChapterId;
    private Integer prevChapterId;
}
