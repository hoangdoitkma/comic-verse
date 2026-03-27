package com.datn.backend.dto.public_api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReadingHistoryInfoDTO {
    private Integer comicId;
    private String title;
    private String thumbnailUrl;
    private Integer lastReadChapterId;
    private BigDecimal lastReadChapterNumber;
    private String lastReadChapterTitle;
}
