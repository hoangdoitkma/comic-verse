package com.datn.backend.repository;

import com.datn.backend.entity.UploadLog;
import com.datn.backend.entity.enums.UploadStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UploadLogRepository extends JpaRepository<UploadLog, Integer> {
    List<UploadLog> findByStatus(UploadStatus status);
    List<UploadLog> findByComicIdOrderByCreatedAtDesc(Integer comicId);
    long countByStatus(UploadStatus status);
    
    java.util.Optional<UploadLog> findFirstByChapterIdOrderByCreatedAtDesc(Integer chapterId);
    
    boolean existsByComicIdAndChapter_ChapterNumberAndStatus(Integer comicId, java.math.BigDecimal chapterNumber, UploadStatus status);
    
    boolean existsByComicIdAndChapter_TitleAndStatus(Integer comicId, String title, UploadStatus status);
}
