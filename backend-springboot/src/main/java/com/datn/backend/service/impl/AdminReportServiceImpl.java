package com.datn.backend.service.impl;

import com.datn.backend.dto.request.HandleReportRequest;
import com.datn.backend.dto.response.ReportResponse;
import com.datn.backend.entity.Comment;
import com.datn.backend.entity.Report;
import com.datn.backend.entity.enums.CommentStatus;
import com.datn.backend.entity.enums.ReportStatus;
import com.datn.backend.repository.CommentRepository;
import com.datn.backend.repository.ReportRepository;
import com.datn.backend.service.AdminReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminReportServiceImpl implements AdminReportService {

    private final ReportRepository reportRepository;
    private final CommentRepository commentRepository;

    @Override
    public Page<ReportResponse> getReports(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Report> reports = reportRepository.findAll(pageable);
        return reports.map(this::mapToReportResponse);
    }

    @Override
    @Transactional
    public void handleReport(Integer reportId, HandleReportRequest request) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));

        report.setStatus(request.getAction());

        // If action is RESOLVED, it usually implies the reported content violates rules, so we delete or hide it.
        // User requested: "Nếu Admin quyết định xóa bình luận vi phạm, cập nhật status của comments thành DELETED hoặc HIDDEN"
        // Let's assume RESOLVED means the comment should be DELETED to simplify but effectively handle it.
        if (request.getAction() == ReportStatus.RESOLVED && report.getComment() != null) {
            Comment comment = report.getComment();
            comment.setStatus(CommentStatus.DELETED);
            commentRepository.save(comment);
        }

        reportRepository.save(report);
    }

    private ReportResponse mapToReportResponse(Report report) {
        return ReportResponse.builder()
                .id(report.getId())
                .reporterId(report.getReporter() != null ? report.getReporter().getId() : null)
                .reporterName(report.getReporter() != null ? report.getReporter().getDisplayName() : null)
                .commentId(report.getComment() != null ? report.getComment().getId() : null)
                .commentContent(report.getComment() != null ? report.getComment().getContent() : null)
                .reason(report.getReason())
                .status(report.getStatus())
                .createdAt(report.getCreatedAt())
                .build();
    }
}
