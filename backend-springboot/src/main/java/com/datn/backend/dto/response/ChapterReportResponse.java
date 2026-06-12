package com.datn.backend.dto.response;

import com.datn.backend.entity.enums.ChapterReportType;
import com.datn.backend.entity.enums.ReportStatus;
import com.datn.backend.entity.enums.AccessType;
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
public class ChapterReportResponse {
    private Integer id;
    private Integer reporterId;
    private String reporterName;
    private Integer chapterId;
    private String chapterTitle;
    private BigDecimal chapterNumber;
    private AccessType chapterAccessType;
    private Integer comicId;
    private String comicTitle;
    private ChapterReportType type;
    private String typeDescription;
    private String reason;
    private String readerMode;
    private Integer pageIndex;
    private Integer pageNumber;
    private Integer paragraphIndex;
    private String contentSnapshot;
    private String pageImageUrlSnapshot;
    private String adminNotes;
    private ReportStatus status;
    private LocalDateTime createdAt;
}
