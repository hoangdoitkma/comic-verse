package com.datn.backend.service;

import com.datn.backend.dto.request.HandleChapterReportRequest;
import com.datn.backend.dto.response.ChapterReportResponse;
import org.springframework.data.domain.Page;

public interface AdminChapterReportService {
    Page<ChapterReportResponse> getAllReports(String status, int page, int size);
    Page<ChapterReportResponse> getReportsByUploader(Integer uploaderId, String status, int page, int size);
    void handleReport(Integer reportId, HandleChapterReportRequest request, Integer actorId, boolean admin);
}
