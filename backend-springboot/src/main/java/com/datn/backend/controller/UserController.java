package com.datn.backend.controller;

import com.datn.backend.dto.response.ApiResponse;
import com.datn.backend.dto.response.UserProfileResponse;
import com.datn.backend.entity.Subscription;
import com.datn.backend.entity.User;
import com.datn.backend.entity.enums.SubscriptionStatus;
import com.datn.backend.repository.SubscriptionRepository;
import com.datn.backend.repository.UserRepository;
import com.datn.backend.security.services.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;

    @GetMapping("/profile")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ApiResponse<UserProfileResponse> getUserProfile(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Subscription> activeSubscriptions = subscriptionRepository.findByUserIdAndStatus(user.getId(), SubscriptionStatus.ACTIVE);
        boolean isVip = false;
        LocalDateTime vipEndDate = null;

        if (!activeSubscriptions.isEmpty()) {
            Subscription sub = activeSubscriptions.get(0);
            if (sub.getEndDate() != null && sub.getEndDate().isAfter(LocalDateTime.now())) {
                isVip = true;
                vipEndDate = sub.getEndDate();
            }
        }

        UserProfileResponse response = UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole() != null ? user.getRole().name() : null)
                .isVip(isVip)
                .vipEndDate(vipEndDate)
                .build();

        return ApiResponse.success(response);
    }
}
