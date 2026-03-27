package com.datn.backend.service;

import com.datn.backend.dto.request.BulkChapterUploadRequest;
import com.datn.backend.dto.request.ChapterRequest;
import com.datn.backend.dto.response.BulkInitResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ChapterService {
    // Create a comic chapter with image pages (old monolith approach — kept for backward compat)
    void createComicChapter(Integer comicId, ChapterRequest request, MultipartFile[] pages);

    // Init single chapter: create Chapter + ChapterPage records (imageUrl=null), return page mappings
    com.datn.backend.dto.response.BulkInitResponse.ChapterInitResult initSingleChapter(Integer comicId, ChapterRequest request);

    // Create a novel chapter with text content
    void createNovelChapter(Integer comicId, ChapterRequest request);

    // Step 1: Init bulk chapters - create Chapter & ChapterPage records (without imageUrl)
    BulkInitResponse initBulkChapters(Integer comicId, List<BulkChapterUploadRequest.ChapterFolder> folders);

    // Step 2: Upload a single page file to S3 and update ChapterPage.imageUrl
    void uploadChapterPageFile(Integer pageId, MultipartFile file);

    // Delete a rejected draft chapter
    void deleteRejectedDraft(Integer chapterId);

    // Get max chapter number for a comic
    java.math.BigDecimal getMaxChapterNumber(Integer comicId);

    // Retrieve chapters as DTOs
    List<ChapterRequest> getChaptersByComic(Integer comicId);

    // Retrieve chapter entities (with IDs)  
    List<com.datn.backend.entity.Chapter> getChapterEntitiesByComic(Integer comicId);

    // Get chapter pages (images) sorted by page number
    List<com.datn.backend.entity.ChapterPage> getChapterPages(Integer chapterId);
}
