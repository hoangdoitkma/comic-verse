package com.datn.backend.dto.response;

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
public class TransactionResponse {
    private Integer id;
    private Long orderCode;
    private String paymentLinkId;
    private BigDecimal amount;
    private String paymentMethod;
    private String status;
    private LocalDateTime createdAt;
    
    // User info
    private Integer userId;
    private String userEmail;
    private String userDisplayName;
    
    // Package info
    private Integer packageId;
    private String packageName;
}
