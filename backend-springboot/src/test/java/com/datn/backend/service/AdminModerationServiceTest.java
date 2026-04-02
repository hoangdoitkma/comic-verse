package com.datn.backend.service;

import com.datn.backend.dto.request.ReviewRequest;
import com.datn.backend.dto.response.UploadLogResponse;
import com.datn.backend.entity.UploadLog;
import com.datn.backend.entity.User;
import com.datn.backend.entity.enums.UploadStatus;
import com.datn.backend.repository.UploadLogRepository;
import com.datn.backend.repository.UserRepository;
import com.datn.backend.service.impl.AdminModerationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminModerationServiceTest {

    @Mock
    private UploadLogRepository uploadLogRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private S3Service s3Service;

    @Mock
    private com.datn.backend.repository.ChapterPageRepository chapterPageRepository;

    @InjectMocks
    private AdminModerationServiceImpl adminModerationService;

    private UploadLog pendingLog;
    private User adminUser;

    @BeforeEach
    void setUp() {
        pendingLog = UploadLog.builder()
                .id(1)
                .status(UploadStatus.PENDING)
                .build();

        adminUser = User.builder()
                .id(99)
                .email("admin@test.com")
                .build();
    }

    @Test
    void testReviewLog_Approve() {
        // Arrange
        ReviewRequest request = new ReviewRequest();
        request.setStatus("APPROVED");

        when(uploadLogRepository.findById(1)).thenReturn(Optional.of(pendingLog));
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(adminUser));
        when(uploadLogRepository.save(any(UploadLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UploadLogResponse response = adminModerationService.reviewLog(1, request, "admin@test.com");

        // Assert
        assertEquals("APPROVED", response.getStatus());
        assertEquals(99, response.getReviewerId());
        assertNotNull(response.getReviewAt());
        verify(uploadLogRepository, times(1)).save(pendingLog);
    }

    @Test
    void testReviewLog_Reject() {
        // Arrange
        ReviewRequest request = new ReviewRequest();
        request.setStatus("REJECTED");
        request.setReason("Inappropriate content");

        when(uploadLogRepository.findById(1)).thenReturn(Optional.of(pendingLog));
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(adminUser));
        when(uploadLogRepository.save(any(UploadLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UploadLogResponse response = adminModerationService.reviewLog(1, request, "admin@test.com");

        // Assert
        assertEquals("REJECTED", response.getStatus());
        assertEquals(99, response.getReviewerId());
        assertNotNull(response.getReviewAt());
        verify(uploadLogRepository, times(1)).save(pendingLog);
    }
}
