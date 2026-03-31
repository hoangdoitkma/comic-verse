package com.datn.backend.service.public_api;

import com.datn.backend.dto.request.ChapterReportRequest;
import com.datn.backend.dto.response.ChapterReportResponse;

public interface PublicChapterReportService {
    ChapterReportResponse addReport(Integer chapterId, Integer userId, ChapterReportRequest request);
}
