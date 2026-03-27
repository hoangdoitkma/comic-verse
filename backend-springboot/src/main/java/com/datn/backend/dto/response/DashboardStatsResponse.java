package com.datn.backend.dto.response;

import com.datn.backend.dto.response.dashboard.RecentComicsDto;
import com.datn.backend.dto.response.dashboard.RecentTransactionsDto;
import com.datn.backend.dto.response.dashboard.RevenueChartDataDto;
import com.datn.backend.dto.response.dashboard.TopComicsDto;
import com.datn.backend.dto.response.dashboard.UploadActivityDataDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    private long totalNewUsers;
    private long totalNewComics;
    private BigDecimal totalRevenue;
    
    // New fields
    private long pendingApprovals;
    private int storageUsed;
    private int storageTotal;

    // KPI Card fields
    private long totalComics;
    private long totalUsers;
    private long activeVipUsers;
    private long vipSoldThisMonth;
    private BigDecimal totalAllTimeRevenue;
    private long onlineUsers;
    
    private List<RevenueChartDataDto> revenueChart;
    private List<UploadActivityDataDto> uploadActivity;
    private List<TopComicsDto> topComics;
    private List<RecentTransactionsDto> recentTransactions;
    private List<RecentComicsDto> recentComics;
}
