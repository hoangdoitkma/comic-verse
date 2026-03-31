package com.datn.backend.dto.response;

import com.datn.backend.entity.enums.ChapterReportType;
import com.datn.backend.entity.enums.ReportStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private Integer comicId;
    private String comicTitle;
    private ChapterReportType type;
    private String typeDescription;
    private String reason;
    private ReportStatus status;
    private LocalDateTime createdAt;
}
