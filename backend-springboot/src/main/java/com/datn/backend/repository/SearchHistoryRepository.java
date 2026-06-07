package com.datn.backend.repository;

import com.datn.backend.entity.SearchHistory;
import com.datn.backend.entity.enums.ContentType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Integer> {

    interface HotSearchProjection {
        String getKeyword();
        ContentType getContentType();
        Long getSearchCount();
        LocalDateTime getLastSearchedAt();
    }

    Optional<SearchHistory> findFirstByUserIdAndNormalizedKeywordAndContentTypeOrderBySearchedAtDesc(
            Integer userId,
            String normalizedKeyword,
            ContentType contentType
    );

    @Query("""
            SELECT sh FROM SearchHistory sh
            WHERE sh.user.id = :userId
              AND sh.normalizedKeyword IS NOT NULL
              AND (:type IS NULL OR sh.contentType = :type)
            ORDER BY sh.searchedAt DESC
            """)
    List<SearchHistory> findUserHistory(
            @Param("userId") Integer userId,
            @Param("type") ContentType type,
            Pageable pageable
    );

    @Query("""
            SELECT sh.normalizedKeyword AS keyword,
                   sh.contentType AS contentType,
                   COUNT(sh.id) AS searchCount,
                   MAX(sh.searchedAt) AS lastSearchedAt
            FROM SearchHistory sh
            WHERE sh.searchedAt >= :since
              AND sh.normalizedKeyword IS NOT NULL
              AND (:type IS NULL OR sh.contentType = :type)
            GROUP BY sh.normalizedKeyword, sh.contentType
            ORDER BY COUNT(sh.id) DESC, MAX(sh.searchedAt) DESC
            """)
    List<HotSearchProjection> findHotSearches(
            @Param("since") LocalDateTime since,
            @Param("type") ContentType type,
            Pageable pageable
    );

    @Modifying
    @Query("""
            DELETE FROM SearchHistory sh
            WHERE sh.user.id = :userId
              AND sh.normalizedKeyword = :normalizedKeyword
              AND (:type IS NULL OR sh.contentType = :type)
            """)
    int deleteUserKeyword(
            @Param("userId") Integer userId,
            @Param("normalizedKeyword") String normalizedKeyword,
            @Param("type") ContentType type
    );

    @Modifying
    @Query("""
            DELETE FROM SearchHistory sh
            WHERE sh.user.id = :userId
              AND (:type IS NULL OR sh.contentType = :type)
            """)
    int deleteUserHistory(
            @Param("userId") Integer userId,
            @Param("type") ContentType type
    );
}
