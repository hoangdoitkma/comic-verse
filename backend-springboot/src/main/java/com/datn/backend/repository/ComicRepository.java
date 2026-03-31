package com.datn.backend.repository;

import com.datn.backend.entity.Comic;
import com.datn.backend.entity.enums.ComicStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComicRepository extends JpaRepository<Comic, Integer> {
    List<Comic> findByStatus(ComicStatus status);
    Optional<Comic> findBySlug(String slug);
    List<Comic> findByCreatedById(Integer createdById);
    boolean existsByAuthorId(Integer authorId);
    long countByCreatedAtBetween(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);

    List<Comic> findTop5ByOrderByViewCountDesc();
    List<Comic> findTop5ByContentTypeOrderByViewCountDesc(com.datn.backend.entity.enums.ContentType type);

    List<Comic> findTop15ByOrderByUpdatedAtDesc();
    List<Comic> findTop15ByContentTypeOrderByUpdatedAtDesc(com.datn.backend.entity.enums.ContentType type);

    List<Comic> findTop10ByOrderByCreatedAtDesc();
    List<Comic> findTop10ByContentTypeOrderByCreatedAtDesc(com.datn.backend.entity.enums.ContentType type);

    @Query(value = "SELECT DATE(created_at) as date, COUNT(*) as amount " +
                   "FROM comics " +
                   "WHERE created_at >= :startDate " +
                   "GROUP BY DATE(created_at)", nativeQuery = true)
    List<Object[]> countComicsByDate(@Param("startDate") java.time.LocalDateTime startDate);
}
