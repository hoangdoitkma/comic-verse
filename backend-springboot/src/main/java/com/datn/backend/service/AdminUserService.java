package com.datn.backend.service;

import com.datn.backend.dto.request.UpdateUserStatusRequest;
import com.datn.backend.dto.response.UserResponse;
import com.datn.backend.entity.enums.Role;
import com.datn.backend.entity.enums.UserStatus;
import org.springframework.data.domain.Page;

public interface AdminUserService {
    Page<UserResponse> getUsers(Role role, UserStatus status, int page, int size);
    void updateUserStatus(Integer userId, UpdateUserStatusRequest request);
}
