package com.datn.backend.dto.request;

import com.datn.backend.entity.enums.ReportStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HandleReportRequest {
    @NotNull(message = "Action is required")
    private ReportStatus action;
}
