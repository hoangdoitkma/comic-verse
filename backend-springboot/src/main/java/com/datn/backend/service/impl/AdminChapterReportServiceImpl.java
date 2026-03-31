package com.datn.backend.service.impl;

import com.datn.backend.dto.request.HandleChapterReportRequest;
import com.datn.backend.dto.response.ChapterReportResponse;
import com.datn.backend.entity.ChapterReport;
import com.datn.backend.exception.ResourceNotFoundException;
import com.datn.backend.repository.ChapterReportRepository;
import com.datn.backend.service.AdminChapterReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminChapterReportServiceImpl implements AdminChapterReportService {

    private final ChapterReportRepository chapterReportRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<ChapterReportResponse> getAllReports(String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        if (status != null && !status.isEmpty()) {
            com.datn.backend.entity.ReportStatus rs = com.datn.backend.entity.ReportStatus.valueOf(status.toUpperCase());
            return chapterReportRepository.findByStatus(rs, pageable).map(this::mapToResponse);
        }
        return chapterReportRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChapterReportResponse> getReportsByUploader(Integer uploaderId, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        if (status != null && !status.isEmpty()) {
            com.datn.backend.entity.ReportStatus rs = com.datn.backend.entity.ReportStatus.valueOf(status.toUpperCase());
            return chapterReportRepository.findByUploaderIdAndStatus(uploaderId, rs, pageable).map(this::mapToResponse);
        }
        return chapterReportRepository.findByUploaderId(uploaderId, pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional
    public void handleReport(Integer reportId, HandleChapterReportRequest request) {
        ChapterReport report = chapterReportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found"));
        report.setStatus(request.getAction());
        chapterReportRepository.save(report);
    }

    private ChapterReportResponse mapToResponse(ChapterReport report) {
        return ChapterReportResponse.builder()
                .id(report.getId())
                .reporterId(report.getReporter().getId())
                .reporterName(report.getReporter().getDisplayName())
                .chapterId(report.getChapter().getId())
                .chapterTitle(report.getChapter().getTitle())
                .comicId(report.getChapter().getComic().getId())
                .comicTitle(report.getChapter().getComic().getTitle())
                .type(report.getType())
                .typeDescription(report.getType().getDescription())
                .reason(report.getReason())
                .status(report.getStatus())
                .createdAt(report.getCreatedAt())
                .build();
    }
}
