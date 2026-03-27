package com.datn.backend.controller;

import com.datn.backend.dto.response.ApiResponse;
import com.datn.backend.dto.response.NotificationResponse;
import com.datn.backend.entity.User;
import com.datn.backend.repository.UserRepository;
import com.datn.backend.security.services.UserDetailsImpl;
import com.datn.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getUserNotifications(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        User user = userRepository.findById(userDetails.getId()).orElseThrow();
        List<NotificationResponse> notifications = notificationService.getUserNotifications(user);
        return ResponseEntity.ok(ApiResponse.success(notifications, "Notifications fetched successfully"));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        User user = userRepository.findById(userDetails.getId()).orElseThrow();
        long count = notificationService.getUnreadCount(user);
        return ResponseEntity.ok(ApiResponse.success(count, "Unread count fetched"));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        User user = userRepository.findById(userDetails.getId()).orElseThrow();
        notificationService.markAsRead(id, user);
        return ResponseEntity.ok(ApiResponse.<Void>success(null, "Notification marked as read"));
    }

    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        User user = userRepository.findById(userDetails.getId()).orElseThrow();
        notificationService.markAllAsRead(user);
        return ResponseEntity.ok(ApiResponse.<Void>success(null, "All notifications marked as read"));
    }
}
