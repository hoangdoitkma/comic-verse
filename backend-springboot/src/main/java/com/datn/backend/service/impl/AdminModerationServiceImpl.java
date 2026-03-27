package com.datn.backend.service.impl;

import com.datn.backend.dto.request.ReviewRequest;
import com.datn.backend.dto.response.UploadLogResponse;
import com.datn.backend.entity.UploadLog;
import com.datn.backend.entity.User;
import com.datn.backend.entity.enums.UploadStatus;
import com.datn.backend.exception.ResourceNotFoundException;
import com.datn.backend.repository.UploadLogRepository;
import com.datn.backend.repository.UserRepository;
import com.datn.backend.service.AdminModerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminModerationServiceImpl implements AdminModerationService {

    private final UploadLogRepository uploadLogRepository;
    private final UserRepository userRepository;
    private final com.datn.backend.service.NotificationService notificationService;
    private final com.datn.backend.service.S3Service s3Service;
    private final com.datn.backend.repository.ChapterPageRepository chapterPageRepository;

    @Override
    public List<UploadLogResponse> getPendingLogs() {
        return uploadLogRepository.findByStatus(UploadStatus.PENDING).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UploadLogResponse reviewLog(Integer logId, ReviewRequest request, String adminEmail) {
        UploadLog log = uploadLogRepository.findById(logId)
                .orElseThrow(() -> new ResourceNotFoundException("UploadLog", "id", logId.toString()));

        if (log.getStatus() != UploadStatus.PENDING) {
            throw new IllegalStateException("UploadLog is already processed");
        }

        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", adminEmail));

        UploadStatus newStatus = "APPROVED".equals(request.getStatus()) ? UploadStatus.APPROVED : UploadStatus.REJECTED;

        log.setStatus(newStatus);
        log.setReviewedBy(admin);
        log.setReviewedAt(LocalDateTime.now());

        UploadLog savedLog = uploadLogRepository.save(log);

        String title = "Kết quả duyệt chương";
        String message = newStatus == UploadStatus.APPROVED 
                ? "Chương " + log.getChapter().getTitle() + " của truyện " + log.getComic().getTitle() + " đã được phê duyệt và hiển thị." 
                : "Chương " + log.getChapter().getTitle() + " bị từ chối.";
        
        if (newStatus == UploadStatus.REJECTED) {
            if (request.getReason() != null && !request.getReason().trim().isEmpty()) {
                message += " Lý do: " + request.getReason();
                log.setRejectReason(request.getReason());
            }

            try {
                com.datn.backend.entity.Chapter chapter = log.getChapter();
                com.datn.backend.entity.Comic comic = log.getComic();
                if (chapter != null && comic != null) {
                    String comicSlug = comic.getSlug();
                    if (comicSlug == null || comicSlug.isBlank()) {
                        comicSlug = comic.getTitle().replaceAll("[^a-zA-Z0-9\\\\-\\\\s]", "").replaceAll("\\\\s+", "-").toLowerCase();
                    }
                    String chapterFolder = "chapter-" + chapter.getChapterNumber().stripTrailingZeros().toPlainString();
                    String timestamp = String.valueOf(System.currentTimeMillis());
                    
                    String oldPrefix = "comics/" + comicSlug + "/chapters/" + chapterFolder + "/";
                    String newPrefix = "comics/" + comicSlug + "/rejected/" + chapterFolder + "_" + timestamp + "/";

                    s3Service.moveFolderS3(oldPrefix, newPrefix);

                    if (chapter.getChapterPages() != null) {
                        for (com.datn.backend.entity.ChapterPage page : chapter.getChapterPages()) {
                            if (page.getImageUrl() != null && page.getImageUrl().contains(oldPrefix)) {
                                page.setImageUrl(page.getImageUrl().replace(oldPrefix, newPrefix));
                                chapterPageRepository.save(page);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // Log but do not block the review process if S3 move fails partially
                System.err.println("Error moving S3 files during rejection quarantine: " + e.getMessage());
            }
        }

        com.datn.backend.entity.enums.NotificationType type = com.datn.backend.entity.enums.NotificationType.SYSTEM;

        notificationService.sendSystemNotification(log.getUploader(), title, message, type, "/uploader/comics");

        return mapToResponse(savedLog);
    }

    private UploadLogResponse mapToResponse(UploadLog log) {
        return UploadLogResponse.builder()
                .id(log.getId())
                .status(log.getStatus().name())
                .uploaderId(log.getUploader() != null ? log.getUploader().getId() : null)
                .uploaderName(log.getUploader() != null ? log.getUploader().getDisplayName() : null)
                .comicId(log.getComic() != null ? log.getComic().getId() : null)
                .comicTitle(log.getComic() != null ? log.getComic().getTitle() : null)
                .chapterId(log.getChapter() != null ? log.getChapter().getId() : null)
                .chapterTitle(log.getChapter() != null ? log.getChapter().getTitle() : null)
                .reviewAt(log.getReviewedAt())
                .reviewerId(log.getReviewedBy() != null ? log.getReviewedBy().getId() : null)
                .createdAt(log.getCreatedAt())
                .build();
    }
}
