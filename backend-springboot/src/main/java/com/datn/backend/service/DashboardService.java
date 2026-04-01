package com.datn.backend.service;

import com.datn.backend.entity.enums.TransactionStatus;
import com.datn.backend.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TransactionRepository transactionRepository;

    public Map<String, Object> getRevenueStats() {
        Map<String, Object> stats = new HashMap<>();

        // 1. Total all-time revenue
        BigDecimal totalRevenue = transactionRepository.sumAmountByStatus(TransactionStatus.SUCCESS);
        stats.put("totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);

        // 2. This month revenue
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1);
        BigDecimal thisMonthRevenue = transactionRepository.sumAmountByStatusAndDateRange(TransactionStatus.SUCCESS, startOfMonth, endOfMonth);
        stats.put("thisMonthRevenue", thisMonthRevenue != null ? thisMonthRevenue : BigDecimal.ZERO);

        // 3. Revenue by Date (last 30 days)
        LocalDateTime last30Days = LocalDateTime.now().minusDays(30);
        List<Object[]> dailyRevenueRaw = transactionRepository.getDailyRevenueByDateRange(last30Days);
        List<Map<String, Object>> revenueByDate = new ArrayList<>();
        for (Object[] row : dailyRevenueRaw) {
            Map<String, Object> map = new HashMap<>();
            map.put("date", row[0].toString());
            map.put("revenue", row[1]);
            revenueByDate.add(map);
        }
        stats.put("revenueByDate", revenueByDate);

        // 4. VIP Package Sales
        List<Object[]> vipSalesRaw = transactionRepository.getVipPackageSales();
        List<Map<String, Object>> vipPackageSales = new ArrayList<>();
        for (Object[] row : vipSalesRaw) {
            Map<String, Object> map = new HashMap<>();
            map.put("name", row[0]);
            map.put("count", row[1]);
            vipPackageSales.add(map);
        }
        stats.put("vipPackageSales", vipPackageSales);

        // 5. Transaction Rates
        List<Object[]> ratesRaw = transactionRepository.getTransactionRates();
        List<Map<String, Object>> transactionRates = new ArrayList<>();
        for (Object[] row : ratesRaw) {
            Map<String, Object> map = new HashMap<>();
            map.put("name", row[0].toString().equals("SUCCESS") ? "Thành công" : (row[0].toString().equals("FAILED") ? "Thất bại" : "Đang chờ"));
            map.put("value", row[1]);
            transactionRates.add(map);
        }
        stats.put("transactionRates", transactionRates);

        return stats;
    }
}
