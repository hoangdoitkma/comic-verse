package com.datn.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class BulkInitResponse {

    private List<ChapterInitResult> chapters;

    @Data
    @Builder
    public static class ChapterInitResult {
        private Integer chapterId;
        private BigDecimal chapterNumber;
        private String folderName;
        private List<PageMapping> pages;
    }

    @Data
    @Builder
    public static class PageMapping {
        private Integer pageId;      // ChapterPage.id in DB
        private Integer pageNumber;
        private String fileName;     // original file name
    }
}
