package com.datn.backend.controller.public_api;

import com.datn.backend.dto.request.ChapterReportRequest;
import com.datn.backend.dto.response.ApiResponse;
import com.datn.backend.dto.response.ChapterReportResponse;
import com.datn.backend.security.services.UserDetailsImpl;
import com.datn.backend.service.public_api.PublicChapterReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicChapterReportController {

    private final PublicChapterReportService publicChapterReportService;

    @PostMapping("/chapters/{chapterId}/reports")
    public ResponseEntity<ApiResponse<ChapterReportResponse>> addReport(
            @PathVariable Integer chapterId,
            @Valid @RequestBody ChapterReportRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl)) {
            return ResponseEntity.status(401).build();
        }
        Integer userId = ((UserDetailsImpl) authentication.getPrincipal()).getId();
        ChapterReportResponse response = publicChapterReportService.addReport(chapterId, userId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
