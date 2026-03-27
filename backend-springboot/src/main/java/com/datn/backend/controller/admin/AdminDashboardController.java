package com.datn.backend.controller.admin;

import com.datn.backend.dto.response.ApiResponse;
import com.datn.backend.dto.response.DashboardStatsResponse;
import com.datn.backend.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping("/summary")
    public ApiResponse<DashboardStatsResponse> getSummaryStats() {
        DashboardStatsResponse stats = adminDashboardService.getSummaryStats();
        return ApiResponse.success(stats);
    }
}
