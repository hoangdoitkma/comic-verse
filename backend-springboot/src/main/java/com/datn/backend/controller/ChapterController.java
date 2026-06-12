package com.datn.backend.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.datn.backend.dto.request.BulkChapterUploadRequest;
import com.datn.backend.dto.request.ChapterAccessTypeUpdateRequest;
import com.datn.backend.dto.request.ChapterRequest;
import com.datn.backend.dto.response.ApiResponse;
import com.datn.backend.dto.response.BulkInitResponse;
import com.datn.backend.dto.response.ChapterAccessTypeUpdateResponse;
import com.datn.backend.repository.UploadLogRepository;
import com.datn.backend.security.services.UserDetailsImpl;
import com.datn.backend.service.ChapterService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/uploader/chapters")
@PreAuthorize("hasAnyRole('UPLOADER', 'ADMIN')")
public class ChapterController {

    @Autowired
    private ChapterService chapterService;

    @Autowired
    private UploadLogRepository uploadLogRepository;

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

    @GetMapping("/{comicId}")
    public ApiResponse<List<Map<String, Object>>> getChapters(@PathVariable Integer comicId) {
        List<com.datn.backend.entity.Chapter> chapterEntities = chapterService.getChapterEntitiesByComic(comicId);
        List<com.datn.backend.entity.UploadLog> logs = uploadLogRepository.findByComicIdOrderByCreatedAtDesc(comicId);
        
        // Group logs by chapter ID, since it's ordered desc, the first one encountered is the latest.
        Map<Integer, String> statusMap = new HashMap<>();
        Map<Integer, String> reasonMap = new HashMap<>();
        for (com.datn.backend.entity.UploadLog log : logs) {
            if (log.getChapter() != null && !statusMap.containsKey(log.getChapter().getId())) {
                statusMap.put(log.getChapter().getId(), log.getStatus().name());
                reasonMap.put(log.getChapter().getId(), log.getRejectReason());
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (com.datn.backend.entity.Chapter ch : chapterEntities) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", ch.getId());
            map.put("chapterNumber", ch.getChapterNumber());
            map.put("title", ch.getTitle());
            map.put("accessType", ch.getAccessType());
            map.put("viewCount", ch.getViewCount());
            map.put("createdAt", ch.getCreatedAt());
            map.put("status", statusMap.getOrDefault(ch.getId(), "APPROVED")); // Default if no log
            map.put("rejectReason", reasonMap.get(ch.getId()));
            result.add(map);
        }
        return ApiResponse.success(result);
    }

    @GetMapping("/view/{chapterId}/pages")
    public ApiResponse<List<Map<String, Object>>> getChapterPages(@PathVariable Integer chapterId) {
        com.datn.backend.entity.Chapter chapter = chapterService.getChapterById(chapterId);
        assertCanViewChapter(chapter);
        List<com.datn.backend.entity.ChapterPage> pages = chapterService.getChapterPages(chapterId);
        List<Map<String, Object>> result = pages.stream().map(p -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("pageNumber", p.getPageNumber());
            map.put("imageUrl", p.getImageUrl());
            return map;
        }).toList();
        return ApiResponse.success(result);
    }

    @GetMapping("/view/{chapterId}/detail")
    public ApiResponse<Map<String, Object>> getChapterDetail(@PathVariable Integer chapterId) {
        com.datn.backend.entity.Chapter ch = chapterService.getChapterById(chapterId);
        assertCanViewChapter(ch);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", ch.getId());
        map.put("title", ch.getTitle());
        map.put("chapterNumber", ch.getChapterNumber());
        map.put("content", ch.getContent());

        List<Map<String, Object>> pages = ch.getChapterPages().stream()
                .sorted((p1, p2) -> Integer.compare(p1.getPageNumber(), p2.getPageNumber()))
                .map(p -> {
                    Map<String, Object> pMap = new LinkedHashMap<>();
                    pMap.put("pageNumber", p.getPageNumber());
                    pMap.put("imageUrl", p.getImageUrl());
                    return pMap;
                }).toList();
        map.put("pages", pages);
        return ApiResponse.success(map);
    }

    private void assertCanViewChapter(com.datn.backend.entity.Chapter chapter) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        if (isAdmin) {
            return;
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserDetailsImpl userDetails)) {
            throw new AccessDeniedException("You do not have permission to view this chapter.");
        }

        Integer ownerId = chapter.getComic() != null && chapter.getComic().getCreatedBy() != null
                ? chapter.getComic().getCreatedBy().getId()
                : null;
        if (ownerId == null || !ownerId.equals(userDetails.getId())) {
            throw new AccessDeniedException("You do not have permission to view this chapter.");
        }
    }

    @GetMapping("/{comicId}/max-chapter-number")
    public ApiResponse<BigDecimal> getMaxChapterNumber(@PathVariable Integer comicId) {
        BigDecimal maxNumber = chapterService.getMaxChapterNumber(comicId);
        return ApiResponse.success(maxNumber);
    }

    @PatchMapping("/access-type")
    public ApiResponse<ChapterAccessTypeUpdateResponse> updateChapterAccessType(
            @Valid @RequestBody ChapterAccessTypeUpdateRequest request) {
        ChapterAccessTypeUpdateResponse result = chapterService.updateChapterAccessTypes(request.getChapterIds(), request.getAccessType());
        return ApiResponse.success(result, "Chapter access type updated successfully");
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
     * Receives JSON metadata, creates Chapter + ChapterPage records in DB (imageUrl
     * = null).
     * Returns mapping of pageId -> fileName so frontend knows where to upload each
     * file.
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
     * Receives one image file, uploads to S3, and updates ChapterPage.imageUrl in
     * DB.
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
