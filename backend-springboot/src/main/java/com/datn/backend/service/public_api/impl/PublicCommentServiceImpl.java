package com.datn.backend.service.public_api.impl;

import com.datn.backend.dto.request.CommentRequest;
import com.datn.backend.dto.response.CommentResponse;
import com.datn.backend.entity.Chapter;
import com.datn.backend.entity.Comment;
import com.datn.backend.entity.User;
import com.datn.backend.entity.enums.CommentStatus;
import com.datn.backend.repository.ChapterRepository;
import com.datn.backend.repository.CommentRepository;
import com.datn.backend.repository.UserRepository;
import com.datn.backend.service.public_api.PublicCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PublicCommentServiceImpl implements PublicCommentService {

    private final CommentRepository commentRepository;
    private final ChapterRepository chapterRepository;
    private final com.datn.backend.repository.ComicRepository comicRepository;
    private final UserRepository userRepository;

    @Override
    public Page<CommentResponse> getCommentsByChapter(Integer chapterId, Pageable pageable) {
        Page<Comment> comments = commentRepository.findByChapterIdAndParentIsNull(chapterId, pageable);
        return comments.map(this::mapToResponse);
    }

    @Override
    public Page<CommentResponse> getCommentsByComic(Integer comicId, Pageable pageable) {
        Page<Comment> comments = commentRepository.findByComicIdAndParentIsNull(comicId, pageable);
        return comments.map(this::mapToResponse);
    }

    @Override
    public Page<CommentResponse> getRepliesByComment(Integer commentId, Pageable pageable) {
        Page<Comment> replies = commentRepository.findByParentId(commentId, pageable);
        return replies.map(this::mapToResponse);
    }

    @Override
    @Transactional
    public CommentResponse addComment(Integer chapterId, Integer userId, CommentRequest request) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new RuntimeException("Chapter not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Comment parent = null;
        if (request.getParentId() != null) {
            parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent comment not found"));
            // increment reply count of parent
            parent.setReplyCount(parent.getReplyCount() + 1);
            commentRepository.save(parent);
        }

        Comment comment = Comment.builder()
                .chapter(chapter)
                .user(user)
                .parent(parent)
                .content(request.getContent())
                .status(CommentStatus.VISIBLE)
                .likeCount(0)
                .replyCount(0)
                .build();

        Comment savedComment = commentRepository.save(comment);
        return mapToResponse(savedComment);
    }

    @Override
    @Transactional
    public CommentResponse addComicComment(Integer comicId, Integer userId, CommentRequest request) {
        com.datn.backend.entity.Comic comic = comicRepository.findById(comicId)
                .orElseThrow(() -> new RuntimeException("Comic not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Comment parent = null;
        if (request.getParentId() != null) {
            parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent comment not found"));
            // increment reply count of parent
            parent.setReplyCount(parent.getReplyCount() + 1);
            commentRepository.save(parent);
        }

        Comment comment = Comment.builder()
                .comic(comic)
                .user(user)
                .parent(parent)
                .content(request.getContent())
                .status(CommentStatus.VISIBLE)
                .likeCount(0)
                .replyCount(0)
                .build();

        Comment savedComment = commentRepository.save(comment);
        return mapToResponse(savedComment);
    }

    private CommentResponse mapToResponse(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .userId(comment.getUser().getId())
                .userDisplayName(comment.getUser().getDisplayName() != null ? comment.getUser().getDisplayName() : comment.getUser().getEmail())
                .userAvatarUrl(comment.getUser().getAvatarUrl())
                .content(comment.getContent())
                .likeCount(comment.getLikeCount())
                .replyCount(comment.getReplyCount())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
