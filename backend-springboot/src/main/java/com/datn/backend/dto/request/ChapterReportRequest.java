package com.datn.backend.dto.request;

import com.datn.backend.entity.enums.ChapterReportType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChapterReportRequest {
    @NotNull(message = "Type is required")
    private ChapterReportType type;
    
    private String reason;
}
