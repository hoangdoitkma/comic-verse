package com.datn.backend.controller.admin;

import com.datn.backend.dto.request.HandleReportRequest;
import com.datn.backend.dto.response.ApiResponse;
import com.datn.backend.dto.response.ReportResponse;
import com.datn.backend.service.AdminReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminReportController {

    private final AdminReportService adminReportService;

    @GetMapping
    public ApiResponse<Page<ReportResponse>> getReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<ReportResponse> reports = adminReportService.getReports(page, size);
        return ApiResponse.success(reports);
    }

    @PutMapping("/{reportId}")
    public ApiResponse<Void> handleReport(
            @PathVariable Integer reportId,
            @Valid @RequestBody HandleReportRequest request) {
        adminReportService.handleReport(reportId, request);
        return ApiResponse.success(null, "Report handled successfully");
    }
}
