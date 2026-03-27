package com.datn.backend.service.impl;

import com.datn.backend.dto.request.UpdateUserStatusRequest;
import com.datn.backend.dto.response.UserResponse;
import com.datn.backend.entity.User;
import com.datn.backend.entity.enums.Role;
import com.datn.backend.entity.enums.UserStatus;
import com.datn.backend.repository.UserRepository;
import com.datn.backend.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;

    @Override
    public Page<UserResponse> getUsers(Role role, UserStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> users = userRepository.findByRoleAndStatus(role, status, pageable);
        return users.map(this::mapToUserResponse);
    }

    @Override
    @Transactional
    public void updateUserStatus(Integer userId, UpdateUserStatusRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setStatus(request.getStatus());
        userRepository.save(user);
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
