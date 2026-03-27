package com.datn.backend.controller;

import com.datn.backend.dto.request.BulkChapterUploadRequest;
import com.datn.backend.dto.request.ChapterRequest;
import com.datn.backend.dto.response.ApiResponse;
import com.datn.backend.dto.response.BulkInitResponse;
import com.datn.backend.service.ChapterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/uploader/chapters")
@PreAuthorize("hasAnyRole('UPLOADER', 'ADMIN')")
public class ChapterController {

    @Autowired
    private ChapterService chapterService;

    // ── Single chapter endpoints (existing) ──────────────────────────────

    @PostMapping("/{comicId}/comic")
    public ApiResponse<Void> createComicChapter(@PathVariable Integer comicId,
                                                @ModelAttribute ChapterRequest request,
                                                @RequestParam("pages") MultipartFile[] pages) {
        chapterService.createComicChapter(comicId, request, pages);
        return ApiResponse.success(null, "Comic chapter created successfully");
    }

    @PostMapping("/{comicId}/novel")
    public ApiResponse<Void> createNovelChapter(@PathVariable Integer comicId,
                                                @RequestBody ChapterRequest request) {
        chapterService.createNovelChapter(comicId, request);
        return ApiResponse.success(null, "Novel chapter created successfully");
    }

    @Autowired
    private com.datn.backend.repository.UploadLogRepository uploadLogRepository;

    @GetMapping("/{comicId}")
    public ApiResponse<List<java.util.Map<String, Object>>> getChapters(@PathVariable Integer comicId) {
        var chapterEntities = chapterService.getChapterEntitiesByComic(comicId);
        var logs = uploadLogRepository.findByComicIdOrderByCreatedAtDesc(comicId);
        
        // Group logs by chapter ID, since it's ordered desc, the first one encountered is the latest.
        java.util.Map<Integer, String> statusMap = new java.util.HashMap<>();
        java.util.Map<Integer, String> reasonMap = new java.util.HashMap<>();
        for (var log : logs) {
            if (log.getChapter() != null && !statusMap.containsKey(log.getChapter().getId())) {
                statusMap.put(log.getChapter().getId(), log.getStatus().name());
                reasonMap.put(log.getChapter().getId(), log.getRejectReason());
            }
        }

        List<java.util.Map<String, Object>> result = new java.util.ArrayList<>();
        for (var ch : chapterEntities) {
            java.util.Map<String, Object> map = new java.util.LinkedHashMap<>();
            map.put("id", ch.getId());
            map.put("chapterNumber", ch.getChapterNumber());
            map.put("title", ch.getTitle());
            map.put("accessType", ch.getAccessType());
            map.put("status", statusMap.getOrDefault(ch.getId(), "APPROVED")); // Default if no log
            map.put("rejectReason", reasonMap.get(ch.getId()));
            result.add(map);
        }
        return ApiResponse.success(result);
    }

    @GetMapping("/view/{chapterId}/pages")
    public ApiResponse<List<java.util.Map<String, Object>>> getChapterPages(@PathVariable Integer chapterId) {
        var pages = chapterService.getChapterPages(chapterId);
        List<java.util.Map<String, Object>> result = pages.stream().map(p -> {
            java.util.Map<String, Object> map = new java.util.LinkedHashMap<>();
            map.put("pageNumber", p.getPageNumber());
            map.put("imageUrl", p.getImageUrl());
            return map;
        }).toList();
        return ApiResponse.success(result);
    }

    @GetMapping("/{comicId}/max-chapter-number")
    public ApiResponse<BigDecimal> getMaxChapterNumber(@PathVariable Integer comicId) {
        BigDecimal maxNumber = chapterService.getMaxChapterNumber(comicId);
        return ApiResponse.success(maxNumber);
    }

    /**
     * Init single chapter: create Chapter + ChapterPage records (imageUrl=null).
     * Returns page mappings (pageId → fileName) for sequential file upload.
     */
    @PostMapping("/{comicId}/init-single")
    public ApiResponse<BulkInitResponse.ChapterInitResult> initSingleChapter(
            @PathVariable Integer comicId,
            @RequestBody ChapterRequest request) {
        BulkInitResponse.ChapterInitResult result = chapterService.initSingleChapter(comicId, request);
        return ApiResponse.success(result, "Chapter initialized successfully");
    }

    // ── Bulk Upload: 2-step "Data First" approach ────────────────────────

    /**
     * Step 1: Init bulk chapters.
     * Receives JSON metadata, creates Chapter + ChapterPage records in DB (imageUrl = null).
     * Returns mapping of pageId -> fileName so frontend knows where to upload each file.
     */
    @PostMapping("/{comicId}/bulk-init")
    public ApiResponse<BulkInitResponse> initBulkChapters(
            @PathVariable Integer comicId,
            @RequestBody BulkChapterUploadRequest request) {
        BulkInitResponse response = chapterService.initBulkChapters(comicId, request.getChapters());
        return ApiResponse.success(response, "Chapters initialized successfully");
    }

    /**
     * Step 2: Upload a single page file.
     * Receives one image file, uploads to S3, and updates ChapterPage.imageUrl in DB.
     * Frontend calls this endpoint in parallel (concurrency limit 3-5).
     */
    @PostMapping("/bulk-upload-page/{pageId}")
    public ApiResponse<Void> uploadChapterPage(
            @PathVariable Integer pageId,
            @RequestParam("file") MultipartFile file) {
        chapterService.uploadChapterPageFile(pageId, file);
        return ApiResponse.success(null, "Page uploaded successfully");
    }

    /**
     * Delete a rejected draft chapter
     */
    @DeleteMapping("/{chapterId}/rejected-draft")
    public ApiResponse<Void> deleteRejectedDraft(@PathVariable Integer chapterId) {
        chapterService.deleteRejectedDraft(chapterId);
        return ApiResponse.success(null, "Bản nháp đã được xóa thành công");
    }
}
