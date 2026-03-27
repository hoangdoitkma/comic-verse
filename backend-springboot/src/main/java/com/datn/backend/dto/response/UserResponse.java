package com.datn.backend.dto.response;

import com.datn.backend.entity.enums.Role;
import com.datn.backend.entity.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Integer id;
    private String email;
    private String displayName;
    private String avatarUrl;
    private Role role;
    private UserStatus status;
    private LocalDateTime createdAt;
}
