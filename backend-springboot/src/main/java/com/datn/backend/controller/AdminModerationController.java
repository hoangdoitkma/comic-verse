package com.datn.backend.controller;

import com.datn.backend.dto.request.ReviewRequest;
import com.datn.backend.dto.response.ApiResponse;
import com.datn.backend.dto.response.UploadLogResponse;
import com.datn.backend.service.AdminModerationService;
import com.datn.backend.service.ComicService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/moderation")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminModerationController {

    private final AdminModerationService adminModerationService;
    private final ComicService comicService;

    @GetMapping("/logs/pending")
    public ApiResponse<List<UploadLogResponse>> getPendingLogs() {
        return ApiResponse.success(adminModerationService.getPendingLogs());
    }

    @PutMapping("/logs/{logId}")
    public ApiResponse<UploadLogResponse> reviewLog(
            @PathVariable Integer logId,
            @Valid @RequestBody ReviewRequest request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String adminEmail = authentication.getName();

        return ApiResponse.success(
                adminModerationService.reviewLog(logId, request, adminEmail),
                "Review processed successfully"
        );
    }

    @GetMapping("/comics/{comicId}")
    public ApiResponse<com.datn.backend.dto.response.ComicResponse> getComicById(@PathVariable Integer comicId) {
        return ApiResponse.success(comicService.getComicById(comicId));
    }
}
