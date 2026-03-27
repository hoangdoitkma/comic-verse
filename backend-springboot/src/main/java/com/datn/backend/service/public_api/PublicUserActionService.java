package com.datn.backend.service.public_api;

import com.datn.backend.dto.public_api.request.ReadingHistoryRequest;

public interface PublicUserActionService {
    void updateReadingHistory(ReadingHistoryRequest request, Integer userId);
}
