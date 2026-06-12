package com.datn.backend.service.public_api.impl;

import com.datn.backend.dto.request.ChapterReportRequest;
import com.datn.backend.dto.response.ChapterReportResponse;
import com.datn.backend.entity.Chapter;
import com.datn.backend.entity.ChapterPage;
import com.datn.backend.entity.ChapterReport;
import com.datn.backend.entity.User;
import com.datn.backend.exception.ResourceNotFoundException;
import com.datn.backend.repository.ChapterReportRepository;
import com.datn.backend.repository.ChapterRepository;
import com.datn.backend.repository.UserRepository;
import com.datn.backend.service.public_api.PublicChapterReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PublicChapterReportServiceImpl implements PublicChapterReportService {

    private final ChapterReportRepository chapterReportRepository;
    private final ChapterRepository chapterRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ChapterReportResponse addReport(Integer chapterId, Integer userId, ChapterReportRequest request) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter not found with id: " + chapterId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        ChapterReport report = ChapterReport.builder()
                .chapter(chapter)
                .reporter(user)
                .type(request.getType())
                .reason(request.getReason())
                .readerMode(normalizeReaderMode(request.getReaderMode(), chapter))
                .pageIndex(request.getPageIndex())
                .paragraphIndex(request.getParagraphIndex())
                .contentSnapshot(trimSnapshot(request.getContentSnapshot()))
                .pageImageUrlSnapshot(resolvePageImageUrlSnapshot(chapter, request))
                .build();

        report = chapterReportRepository.save(report);

        return ChapterReportResponse.builder()
                .id(report.getId())
                .reporterId(user.getId())
                .reporterName(user.getDisplayName())
                .chapterId(chapter.getId())
                .chapterTitle(chapter.getTitle())
                .chapterNumber(chapter.getChapterNumber())
                .chapterAccessType(chapter.getAccessType())
                .comicId(chapter.getComic().getId())
                .comicTitle(chapter.getComic().getTitle())
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

    private String normalizeReaderMode(String readerMode, Chapter chapter) {
        if (readerMode != null && !readerMode.isBlank()) {
            return readerMode.trim().toUpperCase();
        }
        return chapter.getContent() != null && !chapter.getContent().isBlank() ? "NOVEL" : "COMIC";
    }

    private String trimSnapshot(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.length() > 1000 ? trimmed.substring(0, 1000) : trimmed;
    }

    private String resolvePageImageUrlSnapshot(Chapter chapter, ChapterReportRequest request) {
        if (request.getPageImageUrl() != null && !request.getPageImageUrl().isBlank()) {
            return request.getPageImageUrl().trim();
        }
        if (request.getPageIndex() == null || chapter.getChapterPages() == null) {
            return null;
        }
        return chapter.getChapterPages().stream()
                .sorted(java.util.Comparator.comparing(ChapterPage::getPageNumber, java.util.Comparator.nullsLast(Integer::compareTo)))
                .skip(Math.max(0, request.getPageIndex()))
                .map(ChapterPage::getImageUrl)
                .filter(url -> url != null && !url.isBlank())
                .findFirst()
                .orElse(null);
    }
}
