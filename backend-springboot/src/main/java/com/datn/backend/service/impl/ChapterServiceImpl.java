package com.datn.backend.service.impl;

import com.datn.backend.dto.request.BulkChapterUploadRequest;
import com.datn.backend.dto.request.ChapterRequest;
import com.datn.backend.dto.response.BulkInitResponse;
import com.datn.backend.entity.Chapter;
import com.datn.backend.entity.ChapterPage;
import com.datn.backend.entity.Comic;
import com.datn.backend.entity.UploadLog;
import com.datn.backend.entity.User;
import com.datn.backend.entity.enums.UploadStatus;
import com.datn.backend.exception.ResourceNotFoundException;
import com.datn.backend.exception.ResourceConflictException;
import com.datn.backend.repository.ChapterPageRepository;
import com.datn.backend.repository.ChapterRepository;
import com.datn.backend.repository.ComicRepository;
import com.datn.backend.repository.UploadLogRepository;
import com.datn.backend.repository.UserRepository;
import com.datn.backend.service.ChapterService;
import com.datn.backend.service.S3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class ChapterServiceImpl implements ChapterService {

    @Autowired
    private ComicRepository comicRepository;

    @Autowired
    private ChapterRepository chapterRepository;

    @Autowired
    private ChapterPageRepository chapterPageRepository;

    @Autowired
    private UploadLogRepository uploadLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private S3Service s3Service;

    @Autowired
    private com.datn.backend.service.NotificationService notificationService;

    private com.datn.backend.entity.User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    private void notifyAdmins(Comic comic, Chapter chapter, com.datn.backend.entity.User uploader) {
        List<com.datn.backend.entity.User> admins = userRepository.findByRole(com.datn.backend.entity.enums.Role.ADMIN);
        String message = String.format(com.datn.backend.constant.NotificationConstants.MSG_NEW_CHAPTER, uploader.getDisplayName(), chapter.getTitle(), comic.getTitle());
        for (com.datn.backend.entity.User admin : admins) {
            notificationService.sendSystemNotification(admin, com.datn.backend.constant.NotificationConstants.TITLE_NEW_CHAPTER, message, com.datn.backend.entity.enums.NotificationType.NEW_CHAPTER, "/admin/approval-queue");
        }
    }

    @Override
    @Transactional
    public void createComicChapter(Integer comicId, ChapterRequest request, MultipartFile[] pages) {
        Comic comic = comicRepository.findById(comicId)
                .orElseThrow(() -> new ResourceNotFoundException("Comic", "id", comicId));
        Chapter chapter = Chapter.builder()
                .comic(comic)
                .chapterNumber(request.getChapterNumber())
                .title(request.getTitle())
                .accessType(request.getAccessType())
                .sortOrder((int) (comic.getChapters().size() + 1))
                .build();
        // Sort pages by original filename (alpha-numeric) to preserve user's file ordering
        MultipartFile[] sortedPages = Arrays.copyOf(pages, pages.length);
        Arrays.sort(sortedPages, Comparator.comparing(
                f -> f.getOriginalFilename() != null ? f.getOriginalFilename() : "",
                String.CASE_INSENSITIVE_ORDER
        ));

        // Upload pages and create ChapterPage entities
        String folderName = comic.getTitle().replaceAll("[^a-zA-Z0-9\\-\\s]", "").replaceAll("\\s+", "-").toLowerCase();
        List<ChapterPage> pageEntities = new ArrayList<>();
        int pageNumber = 1;
        for (MultipartFile file : sortedPages) {
            String url = s3Service.uploadFile(file, "comics/" + folderName + "/chapters/" + chapter.getChapterNumber());
            ChapterPage cp = ChapterPage.builder()
                    .chapter(chapter)
                    .pageNumber(pageNumber++)
                    .imageUrl(url)
                    .build();
            pageEntities.add(cp);
        }
        chapter.setChapterPages(pageEntities);
        chapterRepository.save(chapter);
        // Create upload log entry
        UploadLog log = UploadLog.builder()
                .uploader(getCurrentUser())
                .comic(comic)
                .chapter(chapter)
                .status(UploadStatus.PENDING)
                .build();
        uploadLogRepository.save(log);
        notifyAdmins(comic, chapter, getCurrentUser());
    }

    // ─── Step 1 for single chapter: Init records, return page mappings ─────
    @Override
    @Transactional
    public BulkInitResponse.ChapterInitResult initSingleChapter(Integer comicId, ChapterRequest request) {
        java.util.Optional<Chapter> existingChapterOpt = chapterRepository.findByComicIdAndChapterNumber(comicId, request.getChapterNumber());
        if (existingChapterOpt.isPresent()) {
            Chapter existingChapter = existingChapterOpt.get();
            java.util.Optional<UploadLog> latestLogOpt = uploadLogRepository.findFirstByChapterIdOrderByCreatedAtDesc(existingChapter.getId());
            if (latestLogOpt.isPresent()) {
                UploadLog latestLog = latestLogOpt.get();
                if (latestLog.getStatus() == UploadStatus.PENDING) {
                    throw new ResourceConflictException("Chương " + request.getChapterNumber() + " đang chờ duyệt từ admin. Không thể tải đè.");
                } else if (latestLog.getStatus() == UploadStatus.APPROVED) {
                    throw new ResourceConflictException("Chương " + request.getChapterNumber() + " đã tồn tại trên hệ thống. Không thể tải đè.");
                } else if (latestLog.getStatus() == UploadStatus.REJECTED) {
                    throw new ResourceConflictException("Chương " + request.getChapterNumber() + " đang bị từ chối. Vui lòng xem lý do và xóa bản nháp cũ trước khi tải lại.");
                }
            } else {
                 throw new ResourceConflictException("Chương " + request.getChapterNumber() + " đã tồn tại trên hệ thống. Không thể tải đè.");
            }
        }

        Comic comic = comicRepository.findById(comicId)
                .orElseThrow(() -> new ResourceNotFoundException("Comic", "id", comicId));

        int currentSortOrder = chapterRepository.findByComicIdOrderBySortOrderAsc(comicId).size();

        Chapter chapter = Chapter.builder()
                .comic(comic)
                .chapterNumber(request.getChapterNumber())
                .title(request.getTitle() != null && !request.getTitle().isBlank()
                        ? request.getTitle()
                        : "Chapter " + request.getChapterNumber())
                .accessType(request.getAccessType())
                .sortOrder(currentSortOrder + 1)
                .build();

        // Create ChapterPage records with page_number from sorted file list index
        List<ChapterPage> pageEntities = new ArrayList<>();
        List<String> pageFileNames = request.getPageFileNames();

        for (int p = 0; p < pageFileNames.size(); p++) {
            ChapterPage cp = ChapterPage.builder()
                    .chapter(chapter)
                    .pageNumber(p + 1)
                    .imageUrl(null) // will be filled when file is uploaded
                    .build();
            pageEntities.add(cp);
        }

        chapter.setChapterPages(pageEntities);
        chapterRepository.saveAndFlush(chapter);

        // After flush, IDs are guaranteed to be generated
        List<ChapterPage> savedPages = chapter.getChapterPages();

        // Upload log
        UploadLog uploadLog = UploadLog.builder()
                .uploader(getCurrentUser())
                .comic(comic)
                .chapter(chapter)
                .status(UploadStatus.PENDING)
                .build();
        uploadLogRepository.save(uploadLog);
        notifyAdmins(comic, chapter, getCurrentUser());

        // Build response mapping: pageId → fileName
        List<BulkInitResponse.PageMapping> pageMappings = new ArrayList<>();
        for (int p = 0; p < savedPages.size(); p++) {
            pageMappings.add(BulkInitResponse.PageMapping.builder()
                    .pageId(savedPages.get(p).getId())
                    .pageNumber(savedPages.get(p).getPageNumber())
                    .fileName(pageFileNames.get(p))
                    .build());
        }

        return BulkInitResponse.ChapterInitResult.builder()
                .chapterId(chapter.getId())
                .chapterNumber(chapter.getChapterNumber())
                .folderName(null)
                .pages(pageMappings)
                .build();
    }

    @Override
    @Transactional
    public void createNovelChapter(Integer comicId, ChapterRequest request) {
        Comic comic = comicRepository.findById(comicId)
                .orElseThrow(() -> new ResourceNotFoundException("Comic", "id", comicId));
        Chapter chapter = Chapter.builder()
                .comic(comic)
                .chapterNumber(request.getChapterNumber())
                .title(request.getTitle())
                .accessType(request.getAccessType())
                .content(request.getContent())
                .sortOrder((int) (comic.getChapters().size() + 1))
                .build();
        chapterRepository.save(chapter);
        // Upload log
        UploadLog log = UploadLog.builder()
                .uploader(getCurrentUser())
                .comic(comic)
                .chapter(chapter)
                .status(UploadStatus.PENDING)
                .build();
        uploadLogRepository.save(log);
        notifyAdmins(comic, chapter, getCurrentUser());
    }

    // ─── Step 1: Init bulk chapters ─── Create Chapter + ChapterPage records WITHOUT imageUrl
    @Override
    @Transactional
    public BulkInitResponse initBulkChapters(Integer comicId,
                                              List<BulkChapterUploadRequest.ChapterFolder> folders) {
        Comic comic = comicRepository.findById(comicId)
                .orElseThrow(() -> new ResourceNotFoundException("Comic", "id", comicId));

        // Get current max chapter number for auto-increment
        BigDecimal maxChapterNumber = chapterRepository.findMaxChapterNumberByComicId(comicId);
        int currentSortOrder = chapterRepository.findByComicIdOrderBySortOrderAsc(comicId).size();

        com.datn.backend.entity.User currentUser = getCurrentUser();

        List<BulkInitResponse.ChapterInitResult> results = new ArrayList<>();

        for (int i = 0; i < folders.size(); i++) {
            BulkChapterUploadRequest.ChapterFolder folder = folders.get(i);
            String titleToCheck = folder.getTitle() != null && !folder.getTitle().isBlank() ? folder.getTitle() : folder.getFolderName();

            java.util.Optional<Chapter> existingChapterOpt = chapterRepository.findByComicIdAndTitle(comicId, titleToCheck);
            if (existingChapterOpt.isPresent()) {
                Chapter existingChapter = existingChapterOpt.get();
                java.util.Optional<UploadLog> latestLogOpt = uploadLogRepository.findFirstByChapterIdOrderByCreatedAtDesc(existingChapter.getId());
                if (latestLogOpt.isPresent()) {
                    UploadLog latestLog = latestLogOpt.get();
                    if (latestLog.getStatus() == UploadStatus.PENDING) {
                        throw new ResourceConflictException("Chương '" + titleToCheck + "' đang chờ duyệt từ admin. Không thể tải đè.");
                    } else if (latestLog.getStatus() == UploadStatus.APPROVED) {
                        throw new ResourceConflictException("Chương '" + titleToCheck + "' đã tồn tại trên hệ thống. Không thể tải đè.");
                    } else if (latestLog.getStatus() == UploadStatus.REJECTED) {
                        throw new ResourceConflictException("Chương '" + titleToCheck + "' đang bị từ chối. Vui lòng xem lý do và xóa bản nháp cũ trước khi tải lại.");
                    }
                } else {
                    throw new ResourceConflictException("Chương '" + titleToCheck + "' đã tồn tại trên hệ thống. Không thể tải đè.");
                }
            }

            // Auto-increment chapter number
            BigDecimal newChapterNumber = maxChapterNumber.add(BigDecimal.valueOf(i + 1));

            Chapter chapter = Chapter.builder()
                    .comic(comic)
                    .chapterNumber(newChapterNumber)
                    .title(titleToCheck)
                    .accessType(folder.getAccessType())
                    .sortOrder(currentSortOrder + i + 1)
                    .build();

            // Create ChapterPage records with page_number from sorted list index
            // imageUrl is NULL at this point — will be filled in Step 2
            List<ChapterPage> pageEntities = new ArrayList<>();
            List<String> pageFileNames = folder.getPageFileNames();

            for (int p = 0; p < pageFileNames.size(); p++) {
                ChapterPage cp = ChapterPage.builder()
                        .chapter(chapter)
                        .pageNumber(p + 1) // page_number from JSON metadata index (1-based)
                        .imageUrl(null)     // will be set when file is uploaded in Step 2
                        .build();
                pageEntities.add(cp);
            }

            chapter.setChapterPages(pageEntities);
            chapterRepository.saveAndFlush(chapter);

            // After flush, IDs are guaranteed to be generated
            List<ChapterPage> savedPages = chapter.getChapterPages();

            // Create upload log with PENDING status
            UploadLog uploadLog = UploadLog.builder()
                    .uploader(currentUser)
                    .comic(comic)
                    .chapter(chapter)
                    .status(UploadStatus.PENDING)
                    .build();
            uploadLogRepository.save(uploadLog);
            notifyAdmins(comic, chapter, currentUser);

            // Build response mapping: pageId -> fileName
            List<BulkInitResponse.PageMapping> pageMappings = new ArrayList<>();
            for (int p = 0; p < savedPages.size(); p++) {
                ChapterPage savedPage = savedPages.get(p);
                pageMappings.add(BulkInitResponse.PageMapping.builder()
                        .pageId(savedPage.getId())
                        .pageNumber(savedPage.getPageNumber())
                        .fileName(pageFileNames.get(p))
                        .build());
            }

            results.add(BulkInitResponse.ChapterInitResult.builder()
                    .chapterId(chapter.getId())
                    .chapterNumber(newChapterNumber)
                    .folderName(folder.getFolderName())
                    .pages(pageMappings)
                    .build());
        }

        return BulkInitResponse.builder().chapters(results).build();
    }

    // ─── Step 2: Upload single page file to S3 and update ChapterPage.imageUrl
    @Override
    @Transactional
    public void uploadChapterPageFile(Integer pageId, MultipartFile file) {
        // Use JOIN FETCH to load ChapterPage + Chapter + Comic in one query
        ChapterPage page = chapterPageRepository.findByIdWithChapterAndComic(pageId)
                .orElseThrow(() -> new ResourceNotFoundException("ChapterPage", "id", pageId));

        Chapter chapter = page.getChapter();
        Comic comic = chapter.getComic();

        // Determine comic slug for S3 path
        String comicSlug = comic.getSlug();
        if (comicSlug == null || comicSlug.isBlank()) {
            comicSlug = comic.getTitle().replaceAll("[^a-zA-Z0-9\\-\\s]", "")
                    .replaceAll("\\s+", "-").toLowerCase();
        }

        // Build folder name from chapter
        String chapterFolder = "chapter-" + chapter.getChapterNumber().stripTrailingZeros().toPlainString();

        // Generate short UUID to prevent file name collisions
        String shortUuid = UUID.randomUUID().toString().substring(0, 8);

        // Get original filename — strip any folder path that browser may include
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null) {
            // Strip folder path: "Chap 2/024.webp" → "024.webp"
            int lastSlash = Math.max(originalFilename.lastIndexOf('/'), originalFilename.lastIndexOf('\\'));
            if (lastSlash >= 0) {
                originalFilename = originalFilename.substring(lastSlash + 1);
            }
        }

        String extension = "";
        String baseName = originalFilename != null ? originalFilename : "page";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            baseName = originalFilename.substring(0, originalFilename.lastIndexOf("."));
        }

        // S3 key: comics/{slug}/chapters/{chapter-folder}/{baseName}-{uuid}.{ext}
        String s3Key = "comics/" + comicSlug + "/chapters/"
                + chapterFolder + "/" + baseName + "-" + shortUuid + extension;

        log.info("Uploading page {} to S3 key: {}", pageId, s3Key);

        String imageUrl = s3Service.uploadFileWithKey(file, s3Key);

        // Update ChapterPage with the S3 URL
        page.setImageUrl(imageUrl);
        chapterPageRepository.save(page);

        log.info("Page {} uploaded successfully: {}", pageId, imageUrl);
    }

    @Override
    @Transactional
    public void deleteRejectedDraft(Integer chapterId) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter", "id", chapterId));

        User currentUser = getCurrentUser();
        
        boolean hasAccess = currentUser.getRole() == com.datn.backend.entity.enums.Role.ADMIN;
        boolean isRejected = false;
        
        List<UploadLog> logs = uploadLogRepository.findByComicIdOrderByCreatedAtDesc(chapter.getComic().getId());
        for (UploadLog logItem : logs) {
            if (logItem.getChapter() != null && logItem.getChapter().getId().equals(chapterId)) {
                if (logItem.getUploader().getId().equals(currentUser.getId())) {
                    hasAccess = true;
                }
                if (logItem.getStatus() == UploadStatus.REJECTED) {
                    isRejected = true;
                }
                break; // Only check the latest log for this chapter
            }
        }

        if (!hasAccess) {
            throw new org.springframework.security.access.AccessDeniedException("You do not have permission to delete this draft.");
        }
        if (!isRejected && currentUser.getRole() != com.datn.backend.entity.enums.Role.ADMIN) {
            throw new IllegalStateException("Chỉ có thể xóa các bản nháp bị từ chối.");
        }

        String prefixToDelete = null;
        if (chapter.getChapterPages() != null && !chapter.getChapterPages().isEmpty()) {
            for (ChapterPage page : chapter.getChapterPages()) {
                if (page.getImageUrl() != null && !page.getImageUrl().isEmpty()) {
                    try {
                        java.net.URL url = new java.net.URL(page.getImageUrl());
                        String path = url.getPath();
                        if (path.startsWith("/")) path = path.substring(1);
                        int lastSlashIndex = path.lastIndexOf("/");
                        if (lastSlashIndex != -1) {
                            prefixToDelete = path.substring(0, lastSlashIndex + 1);
                        }
                    } catch (Exception e) {
                        log.error("Failed to parse URL: {}", page.getImageUrl(), e);
                    }
                    break;
                }
            }
        }

        if (prefixToDelete != null) {
            log.info("Deleting S3 quarantine folder: {}", prefixToDelete);
            s3Service.deleteFolderS3(prefixToDelete);
        }

        for (UploadLog logItem : logs) {
            if (logItem.getChapter() != null && logItem.getChapter().getId().equals(chapterId)) {
                uploadLogRepository.delete(logItem);
            }
        }

        chapterRepository.delete(chapter);
        log.info("Successfully deleted rejected draft chapter {}", chapterId);
    }

    @Override
    public BigDecimal getMaxChapterNumber(Integer comicId) {
        return chapterRepository.findMaxChapterNumberByComicId(comicId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChapterRequest> getChaptersByComic(Integer comicId) {
        List<Chapter> chapters = chapterRepository.findByComicIdOrderBySortOrderAsc(comicId);
        return chapters.stream().map(ch -> {
            ChapterRequest dto = new ChapterRequest();
            dto.setChapterNumber(ch.getChapterNumber());
            dto.setTitle(ch.getTitle());
            dto.setAccessType(ch.getAccessType());
            dto.setContent(ch.getContent());
            return dto;
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Chapter> getChapterEntitiesByComic(Integer comicId) {
        return chapterRepository.findByComicIdOrderBySortOrderAsc(comicId);
    }

    @Transactional(readOnly = true)
    public List<ChapterPage> getChapterPages(Integer chapterId) {
        return chapterPageRepository.findByChapterIdOrderByPageNumberAsc(chapterId);
    }

    @Override
    @Transactional(readOnly = true)
    public Chapter getChapterById(Integer chapterId) {
        return chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter", "id", chapterId));
    }
}
