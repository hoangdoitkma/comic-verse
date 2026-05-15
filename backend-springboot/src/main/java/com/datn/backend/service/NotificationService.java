package com.datn.backend.service;

import com.datn.backend.dto.request.SendNotificationRequest;
import com.datn.backend.dto.response.NotificationResponse;
import com.datn.backend.entity.Notification;
import com.datn.backend.entity.User;
import com.datn.backend.entity.enums.NotificationType;
import com.datn.backend.exception.ResourceNotFoundException;
import com.datn.backend.repository.NotificationRepository;
import com.datn.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional
    public void sendSystemNotification(User targetUser, String title, String message, NotificationType type, String redirectUrl) {
        if (targetUser == null) return;
        
        Notification notification = Notification.builder()
                .user(targetUser)
                .title(title)
                .message(message)
                .type(type)
                .isRead(false)
                .redirectUrl(redirectUrl)
                .isBroadcast(false)
                .batchId(UUID.randomUUID().toString())
                .build();
                
        notificationRepository.save(notification);
    }
    
    @Transactional
    public void sendBroadcastNotification(String title, String message, NotificationType type) {
        // Send to everyone (could be optimized with a batch insert)
        List<User> allUsers = userRepository.findAll();
        String batchId = UUID.randomUUID().toString();
        List<Notification> notifications = allUsers.stream().map(u -> 
            Notification.builder()
                .user(u)
                .title(title)
                .message(message)
                .type(type)
                .isRead(false)
                .isBroadcast(true)
                .batchId(batchId)
                .build()
        ).collect(Collectors.toList());
        
        notificationRepository.saveAll(notifications);
    }
    
    @Transactional
    public void processManualNotification(SendNotificationRequest request) {
        if (request.getIsBroadcast() != null && request.getIsBroadcast()) {
            sendBroadcastNotification(request.getTitle(), request.getMessage(), request.getType());
        } else {
            if (request.getTargetUserId() == null) {
                throw new IllegalArgumentException("Target User ID is required when not broadcasting");
            }
            User target = userRepository.findById(request.getTargetUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + request.getTargetUserId()));
            sendSystemNotification(target, request.getTitle(), request.getMessage(), request.getType(), null);
        }
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getUserNotifications(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(User user) {
        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    @Transactional
    public void markAsRead(Integer notificationId, User user) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
                
        if (!notification.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Cannot mark other user's notification as read");
        }
        
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(User user) {
        notificationRepository.markAllAsReadByUser(user);
    }
    
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getAdminHistory(NotificationType type, Boolean isBroadcast, Pageable pageable) {
        String typeStr = (type != null) ? type.name() : null;
        return notificationRepository.findAdminGroupedHistory(typeStr, isBroadcast, pageable)
                .map(this::mapToResponse);
    }

    private NotificationResponse mapToResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .title(n.getTitle())
                .message(n.getMessage())
                .type(n.getType())
                .isRead(n.getIsRead())
                .redirectUrl(n.getRedirectUrl())
                .createdAt(n.getCreatedAt())
                .targetUserId((n.getIsBroadcast() != null && n.getIsBroadcast()) ? null : (n.getUser() != null ? n.getUser().getId() : null))
                .targetUserName((n.getIsBroadcast() != null && n.getIsBroadcast()) ? "Broadcast" : (n.getUser() != null ? n.getUser().getDisplayName() : "Unknown"))
                .build();
    }
}
