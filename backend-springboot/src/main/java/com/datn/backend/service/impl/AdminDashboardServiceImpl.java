package com.datn.backend.service.impl;

import com.datn.backend.dto.response.DashboardStatsResponse;
import com.datn.backend.dto.response.dashboard.RecentComicsDto;
import com.datn.backend.dto.response.dashboard.RecentTransactionsDto;
import com.datn.backend.dto.response.dashboard.RevenueChartDataDto;
import com.datn.backend.dto.response.dashboard.TopComicsDto;
import com.datn.backend.dto.response.dashboard.UploadActivityDataDto;
import com.datn.backend.entity.Comic;
import com.datn.backend.entity.Transaction;
import com.datn.backend.entity.enums.TransactionStatus;
import com.datn.backend.entity.enums.UploadStatus;
import com.datn.backend.repository.ChapterRepository;
import com.datn.backend.repository.ComicRepository;
import com.datn.backend.repository.TransactionRepository;
import com.datn.backend.repository.UploadLogRepository;
import com.datn.backend.repository.UserRepository;
import com.datn.backend.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final UserRepository userRepository;
    private final ComicRepository comicRepository;
    private final TransactionRepository transactionRepository;
    private final UploadLogRepository uploadLogRepository;
    private final ChapterRepository chapterRepository;

    @Override
    public DashboardStatsResponse getSummaryStats() {
        // 1. Current month overview
        YearMonth currentMonth = YearMonth.now();
        LocalDateTime startDate = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime endDate = currentMonth.plusMonths(1).atDay(1).atStartOfDay();

        long newUsers = userRepository.countByCreatedAtBetween(startDate, endDate);
        long newComics = comicRepository.countByCreatedAtBetween(startDate, endDate);
        
        BigDecimal revenue = transactionRepository.sumAmountByStatusAndDateRange(TransactionStatus.SUCCESS, startDate, endDate);
        if (revenue == null) {
            revenue = BigDecimal.ZERO;
        }

        // 2. Pending Approvals
        long pendingApprovals = uploadLogRepository.countByStatus(UploadStatus.PENDING);

        // 3. Storage (Mocked for now)
        int storageUsed = 450;
        int storageTotal = 1024;

        // 4. Revenue Chart (Last 12 months)
        LocalDateTime twelveMonthsAgo = currentMonth.minusMonths(11).atDay(1).atStartOfDay();
        List<Object[]> monthlyRevenues = transactionRepository.getMonthlyRevenue(TransactionStatus.SUCCESS.name(), twelveMonthsAgo);
        
        List<RevenueChartDataDto> revenueChart = new ArrayList<>();
        for (int i = 11; i >= 0; i--) {
            YearMonth ym = YearMonth.now().minusMonths(i);
            revenueChart.add(new RevenueChartDataDto("Tháng " + ym.getMonthValue(), BigDecimal.ZERO));
        }
        
        for (Object[] row : monthlyRevenues) {
            int year = ((Number) row[0]).intValue();
            int month = ((Number) row[1]).intValue();
            BigDecimal amount = (BigDecimal) row[2];
            
            for (RevenueChartDataDto rc : revenueChart) {
                if (rc.getName().equals("Tháng " + month)) {
                    rc.setRevenue(amount);
                }
            }
        }

        // 5. Upload Activity (Last 7 days)
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(6).withHour(0).withMinute(0).withSecond(0).withNano(0);
        List<Object[]> dailyComics = comicRepository.countComicsByDate(sevenDaysAgo);
        List<Object[]> dailyChapters = chapterRepository.countChaptersByDate(sevenDaysAgo);
        
        List<UploadActivityDataDto> uploadActivity = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDateTime date = LocalDateTime.now().minusDays(i);
            String dayName = "T" + (date.getDayOfWeek().getValue() + 1);
            if (date.getDayOfWeek().getValue() == 7) {
                dayName = "CN";
            }
            uploadActivity.add(new UploadActivityDataDto(dayName, 0, 0));
        }
        
        for (Object[] row : dailyComics) {
            java.sql.Date sqlDate = (java.sql.Date) row[0];
            long count = ((Number) row[1]).longValue();
            LocalDateTime rowDate = sqlDate.toLocalDate().atStartOfDay();
            String dayName = "T" + (rowDate.getDayOfWeek().getValue() + 1);
            if (rowDate.getDayOfWeek().getValue() == 7) dayName = "CN";
            
            for (UploadActivityDataDto dto : uploadActivity) {
                if (dto.getName().equals(dayName)) {
                    dto.setComics(count);
                }
            }
        }
        
        for (Object[] row : dailyChapters) {
            java.sql.Date sqlDate = (java.sql.Date) row[0];
            long count = ((Number) row[1]).longValue();
            LocalDateTime rowDate = sqlDate.toLocalDate().atStartOfDay();
            String dayName = "T" + (rowDate.getDayOfWeek().getValue() + 1);
            if (rowDate.getDayOfWeek().getValue() == 7) dayName = "CN";
            
            for (UploadActivityDataDto dto : uploadActivity) {
                if (dto.getName().equals(dayName)) {
                    dto.setChapters(count);
                }
            }
        }

        // 6. Top Comics
        List<Comic> topVipComics = comicRepository.findTop5ByOrderByViewCountDesc();
        List<TopComicsDto> topComics = topVipComics.stream().map(c -> 
            TopComicsDto.builder()
                .id(c.getId())
                .name(c.getTitle())
                .thumbnail(c.getThumbnailUrl())
                .views(c.getViewCount() != null ? c.getViewCount() : 0)
                .revenue(BigDecimal.ZERO) // Optional
                .build()
        ).collect(Collectors.toList());

        // 7. Recent Transactions
        List<Transaction> recentTransactionsEntities = transactionRepository.findTop5ByStatusOrderByCreatedAtDesc(TransactionStatus.SUCCESS);
        List<RecentTransactionsDto> recentTransactions = recentTransactionsEntities.stream().map(t -> {
            String packageName = "Unknown";
            String packageType = "Normal";
            if (t.getVipPackage() != null) {
                packageName = t.getVipPackage().getName();
                
                // Extract package type simply from name for UI badge
                if (packageName.toLowerCase().contains("diamond")) packageType = "Diamond";
                else if (packageName.toLowerCase().contains("gold")) packageType = "Gold";
                else if (packageName.toLowerCase().contains("silver")) packageType = "Silver";
                else packageType = "VIP";
            }

            return RecentTransactionsDto.builder()
                .id("TRX-" + t.getId())
                .user(t.getUser().getDisplayName() != null ? t.getUser().getDisplayName() : t.getUser().getEmail())
                .packageName(packageName)
                .type(packageType)
                .amount(t.getAmount())
                .time(t.getCreatedAt())
                .build();
        }).collect(Collectors.toList());

        // 8. Recent Comics (last 15 updated)
        List<Comic> recentComicsEntities = comicRepository.findTop15ByOrderByUpdatedAtDesc();
        List<RecentComicsDto> recentComics = recentComicsEntities.stream().map(c ->
            RecentComicsDto.builder()
                .id(c.getId())
                .title(c.getTitle())
                .thumbnail(c.getThumbnailUrl())
                .uploaderName(c.getCreatedBy() != null
                    ? (c.getCreatedBy().getDisplayName() != null
                        ? c.getCreatedBy().getDisplayName()
                        : c.getCreatedBy().getEmail())
                    : "Unknown")
                .status(c.getStatus() != null ? c.getStatus().name() : "ONGOING")
                .updatedAt(c.getUpdatedAt())
                .build()
        ).collect(Collectors.toList());

        // 9. KPI Card Metrics
        long totalComics = comicRepository.count();
        long totalUsers = userRepository.count();
        
        // Active VIP users = distinct users who have at least one successful VIP transaction
        long activeVipUsers = transactionRepository.countDistinctUserByStatus(TransactionStatus.SUCCESS);
        
        // VIP packages sold this month
        long vipSoldThisMonth = transactionRepository.countByStatusAndCreatedAtBetween(
                TransactionStatus.SUCCESS, startDate, endDate);
        
        // All-time revenue
        BigDecimal totalAllTimeRevenue = transactionRepository.sumAmountByStatus(TransactionStatus.SUCCESS);
        if (totalAllTimeRevenue == null) {
            totalAllTimeRevenue = BigDecimal.ZERO;
        }
        
        // Online users (mocked)
        long onlineUsers = 45;

        return DashboardStatsResponse.builder()
                .totalNewUsers(newUsers)
                .totalNewComics(newComics)
                .totalRevenue(revenue)
                .pendingApprovals(pendingApprovals)
                .storageUsed(storageUsed)
                .storageTotal(storageTotal)
                .totalComics(totalComics)
                .totalUsers(totalUsers)
                .activeVipUsers(activeVipUsers)
                .vipSoldThisMonth(vipSoldThisMonth)
                .totalAllTimeRevenue(totalAllTimeRevenue)
                .onlineUsers(onlineUsers)
                .revenueChart(revenueChart)
                .uploadActivity(uploadActivity)
                .topComics(topComics)
                .recentTransactions(recentTransactions)
                .recentComics(recentComics)
                .build();
    }
}
