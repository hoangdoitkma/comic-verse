package com.datn.backend.service;

import com.datn.backend.dto.request.ReviewRequest;
import com.datn.backend.dto.response.UploadLogResponse;

import java.util.List;

public interface AdminModerationService {
    List<UploadLogResponse> getPendingLogs();
    UploadLogResponse reviewLog(Integer logId, ReviewRequest request, String adminEmail);
}
