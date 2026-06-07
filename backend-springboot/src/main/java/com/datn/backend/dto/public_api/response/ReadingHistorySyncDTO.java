package com.datn.backend.dto.public_api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReadingHistorySyncDTO {
    private Integer comicId;
    private String slug;
    private String title;
    private String thumbnailUrl;
    private String authorName;
    private Long viewCount;
    private String contentType;
    private Integer chapterId;
    private String chapterTitle;
    private Integer lastPage;
    private Long updatedAtMillis;
}
