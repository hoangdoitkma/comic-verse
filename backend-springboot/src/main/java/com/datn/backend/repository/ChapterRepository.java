package com.datn.backend.repository;

import com.datn.backend.entity.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ChapterRepository extends JpaRepository<Chapter, Integer> {
    List<Chapter> findByComicIdOrderBySortOrderAsc(Integer comicId);

    java.util.Optional<Chapter> findByComicIdAndTitle(Integer comicId, String title);

    java.util.Optional<Chapter> findByComicIdAndChapterNumber(Integer comicId, java.math.BigDecimal chapterNumber);

    @Query("SELECT COALESCE(MAX(c.chapterNumber), 0) FROM Chapter c WHERE c.comic.id = :comicId")
    BigDecimal findMaxChapterNumberByComicId(@Param("comicId") Integer comicId);

    long countByComicId(Integer comicId);

    long countByCreatedAtBetween(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);

    @Query(value = "SELECT DATE(created_at) as date, COUNT(*) as amount " +
                   "FROM chapters " +
                   "WHERE created_at >= :startDate " +
                   "GROUP BY DATE(created_at)", nativeQuery = true)
    List<Object[]> countChaptersByDate(@Param("startDate") java.time.LocalDateTime startDate);
}
