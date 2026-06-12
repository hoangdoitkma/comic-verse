package com.datn.backend.repository;

import com.datn.backend.entity.Comic;
import com.datn.backend.entity.enums.ComicStatus;
import com.datn.backend.entity.enums.OriginCountry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComicRepository extends JpaRepository<Comic, Integer> {
    List<Comic> findByStatusAndIsDeletedFalse(ComicStatus status);
    Optional<Comic> findBySlugAndIsDeletedFalse(String slug);
    List<Comic> findByCreatedByIdAndIsDeletedFalse(Integer createdById);
    List<Comic> findByIdInAndIsDeletedFalse(List<Integer> ids);
    boolean existsByAuthorIdAndIsDeletedFalse(Integer authorId);
    List<Comic> findByAuthorIdAndIsDeletedFalse(Integer authorId);
    long countByCreatedAtBetweenAndIsDeletedFalse(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);

    List<Comic> findTop5ByIsDeletedFalseOrderByViewCountDesc();
    List<Comic> findTop5ByContentTypeAndIsDeletedFalseOrderByViewCountDesc(com.datn.backend.entity.enums.ContentType type);

    List<Comic> findTop15ByIsDeletedFalseOrderByUpdatedAtDesc();
    List<Comic> findTop15ByContentTypeAndIsDeletedFalseOrderByUpdatedAtDesc(com.datn.backend.entity.enums.ContentType type);

    List<Comic> findTop10ByIsDeletedFalseOrderByCreatedAtDesc();
    List<Comic> findTop10ByContentTypeAndIsDeletedFalseOrderByCreatedAtDesc(com.datn.backend.entity.enums.ContentType type);

    @Query(value = "SELECT DATE(created_at) as date, COUNT(*) as amount " +
                   "FROM comics " +
                   "WHERE created_at >= :startDate AND is_deleted = false " +
                   "GROUP BY DATE(created_at)", nativeQuery = true)
    List<Object[]> countComicsByDate(@Param("startDate") java.time.LocalDateTime startDate);

    @Query("""
            SELECT DISTINCT c FROM Comic c
            LEFT JOIN c.author a
            LEFT JOIN c.comicGenres cg
            LEFT JOIN cg.genre g
            WHERE c.isDeleted = false
              AND (:type IS NULL OR c.contentType = :type)
              AND (:country IS NULL OR c.originCountry = :country)
              AND (:status IS NULL OR c.status = :status)
              AND (:genreId IS NULL OR g.id = :genreId)
              AND (
                    :keyword IS NULL
                    OR LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(c.slug) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(a.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(g.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
            """)
    Page<Comic> searchPublicComics(@Param("keyword") String keyword,
                                   @Param("type") com.datn.backend.entity.enums.ContentType type,
                                   @Param("country") OriginCountry country,
                                   @Param("genreId") Integer genreId,
                                   @Param("status") ComicStatus status,
                                   Pageable pageable);

    // Dành cho Admin: Lấy tất cả truyện kể cả truyện đã xoá (nếu cần quản lý recycle bin)
    // Spring Data JPA sẽ tự tạo query. (Tuỳ chọn: findAll() mặc định lấy hết)
    // public Page<Comic> findAll(Pageable pageable);
}
