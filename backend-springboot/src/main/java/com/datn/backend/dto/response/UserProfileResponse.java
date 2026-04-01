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
public class UserProfileResponse {
    private Integer id;
    private String email;
    private String displayName;
    private String avatarUrl;
    private String role;
    private boolean isVip;
    private LocalDateTime vipEndDate;
}
