package com.datn.backend.service;

import com.datn.backend.dto.request.HandleReportRequest;
import com.datn.backend.dto.response.ReportResponse;
import org.springframework.data.domain.Page;

public interface AdminReportService {
    Page<ReportResponse> getReports(int page, int size);
    void handleReport(Integer reportId, HandleReportRequest request);
}
