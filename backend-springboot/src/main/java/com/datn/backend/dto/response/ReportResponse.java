package com.datn.backend.dto.response;

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
public class ReportResponse {
    private Integer id;
    private Integer reporterId;
    private String reporterName;
    private Integer commentId;
    private String commentContent;
    private String reason;
    private ReportStatus status;
    private LocalDateTime createdAt;
}
