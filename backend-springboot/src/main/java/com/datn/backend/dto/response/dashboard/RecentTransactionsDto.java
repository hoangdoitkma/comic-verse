package com.datn.backend.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentTransactionsDto {
    private String id;
    private String user;
    private String packageName;
    private String type;
    private LocalDateTime time;
    private BigDecimal amount;
}
