package com.datn.backend.controller;

import com.datn.backend.dto.request.SendNotificationRequest;
import com.datn.backend.dto.response.ApiResponse;
import com.datn.backend.dto.response.NotificationResponse;
import com.datn.backend.service.NotificationService;
import com.datn.backend.entity.enums.NotificationType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/notifications")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@RequiredArgsConstructor
public class AdminNotificationController {

    private final NotificationService notificationService;

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<Void>> sendNotification(
            @RequestBody @Valid SendNotificationRequest request) {
        
        notificationService.processManualNotification(request);
        return ResponseEntity.ok(ApiResponse.<Void>success(null, "Notification sent successfully"));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getNotificationHistory(
            @RequestParam(required = false) NotificationType type,
            @RequestParam(required = false) Boolean isBroadcast,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Page<NotificationResponse> history = notificationService.getAdminHistory(type, isBroadcast, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success(history, "Notification history fetched"));
    }
}
