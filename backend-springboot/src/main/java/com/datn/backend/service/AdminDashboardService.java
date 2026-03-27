package com.datn.backend.service;

import com.datn.backend.dto.response.DashboardStatsResponse;

public interface AdminDashboardService {
    DashboardStatsResponse getSummaryStats();
}
