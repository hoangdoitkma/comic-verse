package com.datn.backend.controller.admin;

import com.datn.backend.dto.request.UpdateUserStatusRequest;
import com.datn.backend.dto.response.ApiResponse;
import com.datn.backend.dto.response.UserResponse;
import com.datn.backend.entity.enums.Role;
import com.datn.backend.entity.enums.UserStatus;
import com.datn.backend.service.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ApiResponse<Page<UserResponse>> getUsers(
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<UserResponse> users = adminUserService.getUsers(role, status, page, size);
        return ApiResponse.success(users);
    }

    @PutMapping("/{userId}/status")
    public ApiResponse<Void> updateUserStatus(
            @PathVariable Integer userId,
            @Valid @RequestBody UpdateUserStatusRequest request) {
        adminUserService.updateUserStatus(userId, request);
        return ApiResponse.success(null, "User status updated successfully");
    }
}
