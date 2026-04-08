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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.datn.backend.dto.request.ChangePasswordRequest;
import com.datn.backend.dto.request.UpdateProfileRequest;
import com.datn.backend.service.S3Service;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final S3Service s3Service;
    private final PasswordEncoder passwordEncoder;

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

    @PutMapping("/profile")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ApiResponse<Object> updateProfile(@RequestBody UpdateProfileRequest request, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setDisplayName(request.getDisplayName());
        userRepository.save(user);
        return ApiResponse.success(null, "Cập nhật thông tin thành công");
    }

    @PostMapping("/profile/avatar")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ApiResponse<String> uploadAvatar(@RequestParam("file") MultipartFile file, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        String avatarUrl = s3Service.uploadFile(file, "avatars");
        user.setAvatarUrl(avatarUrl);
        userRepository.save(user);
        return ApiResponse.success(avatarUrl, "Cập nhật ảnh đại diện thành công");
    }

    @PutMapping("/password")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ApiResponse<Object> changePassword(@RequestBody ChangePasswordRequest request, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            return ApiResponse.error(400, "Mật khẩu cũ không chính xác");
        }
        
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return ApiResponse.success(null, "Đổi mật khẩu thành công");
    }
}
