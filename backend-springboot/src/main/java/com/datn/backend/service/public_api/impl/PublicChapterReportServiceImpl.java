package com.datn.backend.service.public_api.impl;

import com.datn.backend.dto.request.ChapterReportRequest;
import com.datn.backend.dto.response.ChapterReportResponse;
import com.datn.backend.entity.Chapter;
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
                .build();

        report = chapterReportRepository.save(report);

        return ChapterReportResponse.builder()
                .id(report.getId())
                .reporterId(user.getId())
                .reporterName(user.getDisplayName())
                .chapterId(chapter.getId())
                .chapterTitle(chapter.getTitle())
                .comicId(chapter.getComic().getId())
                .comicTitle(chapter.getComic().getTitle())
                .type(report.getType())
                .typeDescription(report.getType().getDescription())
                .reason(report.getReason())
                .status(report.getStatus())
                .createdAt(report.getCreatedAt())
                .build();
    }
}
