package com.datn.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {
    private Integer id;
    
    // Minimal user info
    private Integer userId;
    private String userDisplayName;
    private String userAvatarUrl;
    
    private String content;
    private Integer likeCount;
    private Integer replyCount;
    private LocalDateTime createdAt;
}
