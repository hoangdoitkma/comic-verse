package com.datn.backend.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueChartDataDto {
    private String name; // e.g. "Tháng 1"
    private BigDecimal revenue;
}
