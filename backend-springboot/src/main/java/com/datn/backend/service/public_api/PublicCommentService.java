package com.datn.backend.service.public_api;

import com.datn.backend.dto.request.CommentRequest;
import com.datn.backend.dto.response.CommentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PublicCommentService {
    Page<CommentResponse> getCommentsByChapter(Integer chapterId, Pageable pageable);
    
    Page<CommentResponse> getCommentsByComic(Integer comicId, Pageable pageable);

    Page<CommentResponse> getRepliesByComment(Integer commentId, Pageable pageable);
    
    CommentResponse addComment(Integer chapterId, Integer userId, CommentRequest request);
    
    CommentResponse addComicComment(Integer comicId, Integer userId, CommentRequest request);
}
