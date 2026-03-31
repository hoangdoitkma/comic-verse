package com.datn.backend.repository;

import com.datn.backend.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {

    // Fetch root comments for a specific chapter
    Page<Comment> findByChapterIdAndParentIsNull(Integer chapterId, Pageable pageable);

    // Fetch root comments for a specific comic
    Page<Comment> findByComicIdAndParentIsNull(Integer comicId, Pageable pageable);

    // Fetch replies for a specific parent comment
    Page<Comment> findByParentId(Integer parentId, Pageable pageable);
}
