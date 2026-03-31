package com.datn.backend.controller.public_api;

import com.datn.backend.dto.request.CommentRequest;
import com.datn.backend.dto.response.ApiResponse;
import com.datn.backend.dto.response.CommentResponse;
import com.datn.backend.security.services.UserDetailsImpl;
import com.datn.backend.service.public_api.PublicCommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PublicCommentController {

    private final PublicCommentService publicCommentService;

    @GetMapping("/chapters/{chapterId}/comments")
    public ResponseEntity<ApiResponse<Page<CommentResponse>>> getCommentsByChapter(
            @PathVariable Integer chapterId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(publicCommentService.getCommentsByChapter(chapterId, pageable)));
    }

    @GetMapping("/comments/{commentId}/replies")
    public ResponseEntity<ApiResponse<Page<CommentResponse>>> getRepliesByComment(
            @PathVariable Integer commentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(publicCommentService.getRepliesByComment(commentId, pageable)));
    }

    @PostMapping("/chapters/{chapterId}/comments")
    public ResponseEntity<ApiResponse<CommentResponse>> addComment(
            @PathVariable Integer chapterId,
            @Valid @RequestBody CommentRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl)) {
            return ResponseEntity.status(401).build();
        }
        Integer userId = ((UserDetailsImpl) authentication.getPrincipal()).getId();
        CommentResponse response = publicCommentService.addComment(chapterId, userId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    @GetMapping("/comics/{comicId}/comments")
    public ResponseEntity<ApiResponse<Page<CommentResponse>>> getCommentsByComic(
            @PathVariable Integer comicId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(publicCommentService.getCommentsByComic(comicId, pageable)));
    }

    @PostMapping("/comics/{comicId}/comments")
    public ResponseEntity<ApiResponse<CommentResponse>> addComicComment(
            @PathVariable Integer comicId,
            @Valid @RequestBody CommentRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl)) {
            return ResponseEntity.status(401).build();
        }
        Integer userId = ((UserDetailsImpl) authentication.getPrincipal()).getId();
        CommentResponse response = publicCommentService.addComicComment(comicId, userId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
