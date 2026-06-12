package com.datn.backend.service.impl;

import com.datn.backend.dto.request.HandleChapterReportRequest;
import com.datn.backend.dto.response.ChapterReportResponse;
import com.datn.backend.entity.ChapterReport;
import com.datn.backend.entity.enums.ReportStatus;
import com.datn.backend.exception.ResourceNotFoundException;
import com.datn.backend.repository.ChapterReportRepository;
import com.datn.backend.service.AdminChapterReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
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
            if ("HANDLED".equalsIgnoreCase(status)) {
                return chapterReportRepository.findByStatusIn(java.util.List.of(ReportStatus.RESOLVED, ReportStatus.REJECTED), pageable)
                        .map(this::mapToResponse);
            }
            ReportStatus rs = ReportStatus.valueOf(status.toUpperCase());
            return chapterReportRepository.findByStatus(rs, pageable).map(this::mapToResponse);
        }
        return chapterReportRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChapterReportResponse> getReportsByUploader(Integer uploaderId, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        if (status != null && !status.isEmpty()) {
            if ("HANDLED".equalsIgnoreCase(status)) {
                return chapterReportRepository.findByUploaderIdAndStatusIn(uploaderId, java.util.List.of(ReportStatus.RESOLVED, ReportStatus.REJECTED), pageable)
                        .map(this::mapToResponse);
            }
            ReportStatus rs = ReportStatus.valueOf(status.toUpperCase());
            return chapterReportRepository.findByUploaderIdAndStatus(uploaderId, rs, pageable).map(this::mapToResponse);
        }
        return chapterReportRepository.findByUploaderId(uploaderId, pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional
    public void handleReport(Integer reportId, HandleChapterReportRequest request, Integer actorId, boolean admin) {
        ChapterReport report = chapterReportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found"));
        if (!admin) {
            Integer ownerId = report.getChapter().getComic().getCreatedBy() != null
                    ? report.getChapter().getComic().getCreatedBy().getId()
                    : null;
            if (ownerId == null || !ownerId.equals(actorId)) {
                throw new AccessDeniedException("You do not have permission to handle this report.");
            }
        }
        report.setStatus(request.getAction());
        report.setAdminNotes(request.getAdminNotes());
        chapterReportRepository.save(report);
    }

    private ChapterReportResponse mapToResponse(ChapterReport report) {
        return ChapterReportResponse.builder()
                .id(report.getId())
                .reporterId(report.getReporter().getId())
                .reporterName(report.getReporter().getDisplayName())
                .chapterId(report.getChapter().getId())
                .chapterTitle(report.getChapter().getTitle())
                .chapterNumber(report.getChapter().getChapterNumber())
                .chapterAccessType(report.getChapter().getAccessType())
                .comicId(report.getChapter().getComic().getId())
                .comicTitle(report.getChapter().getComic().getTitle())
                .type(report.getType())
                .typeDescription(report.getType().getDescription())
                .reason(report.getReason())
                .readerMode(report.getReaderMode())
                .pageIndex(report.getPageIndex())
                .pageNumber(report.getPageIndex() != null ? report.getPageIndex() + 1 : null)
                .paragraphIndex(report.getParagraphIndex())
                .contentSnapshot(report.getContentSnapshot())
                .pageImageUrlSnapshot(report.getPageImageUrlSnapshot())
                .adminNotes(report.getAdminNotes())
                .status(report.getStatus())
                .createdAt(report.getCreatedAt())
                .build();
    }
}
