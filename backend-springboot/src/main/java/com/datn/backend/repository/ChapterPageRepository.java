package com.datn.backend.repository;

import com.datn.backend.entity.ChapterPage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChapterPageRepository extends JpaRepository<ChapterPage, Integer> {

    @Query("SELECT cp FROM ChapterPage cp " +
            "JOIN FETCH cp.chapter c " +
            "JOIN FETCH c.comic " +
            "WHERE cp.id = :pageId")
    Optional<ChapterPage> findByIdWithChapterAndComic(@Param("pageId") Integer pageId);

    List<ChapterPage> findByChapterIdOrderByPageNumberAsc(Integer chapterId);
}
