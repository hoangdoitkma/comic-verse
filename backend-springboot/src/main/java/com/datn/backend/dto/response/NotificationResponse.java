package com.datn.backend.dto.response;

import com.datn.backend.entity.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private Integer id;
    private String title;
    private String message;
    private NotificationType type;
    private Boolean isRead;
    private String redirectUrl;
    private LocalDateTime createdAt;
    
    // For admin view history
    private Integer targetUserId;
    private String targetUserName;
}
