package com.datn.backend.controller.admin;

import com.datn.backend.dto.request.HandleChapterReportRequest;
import com.datn.backend.dto.response.ApiResponse;
import com.datn.backend.dto.response.ChapterReportResponse;
import com.datn.backend.security.services.UserDetailsImpl;
import com.datn.backend.service.AdminChapterReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AdminChapterReportController {

    private final AdminChapterReportService adminChapterReportService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/chapter-reports")
    public ApiResponse<Page<ChapterReportResponse>> getAllReports(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(adminChapterReportService.getAllReports(status, page, size));
    }

    @PreAuthorize("hasRole('UPLOADER')")
    @GetMapping("/uploader/chapter-reports")
    public ApiResponse<Page<ChapterReportResponse>> getUploaderReports(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Integer uploaderId = ((UserDetailsImpl) authentication.getPrincipal()).getId();
        return ApiResponse.success(adminChapterReportService.getReportsByUploader(uploaderId, status, page, size));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'UPLOADER')")
    @PutMapping({"/admin/chapter-reports/{id}/status", "/uploader/chapter-reports/{id}/status"})
    public ApiResponse<Void> handleReport(
            @PathVariable Integer id,
            @Valid @RequestBody HandleChapterReportRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Integer actorId = ((UserDetailsImpl) authentication.getPrincipal()).getId();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        adminChapterReportService.handleReport(id, request, actorId, isAdmin);
        return ApiResponse.success(null, "Cập nhật trạng thái thành công");
    }
}
