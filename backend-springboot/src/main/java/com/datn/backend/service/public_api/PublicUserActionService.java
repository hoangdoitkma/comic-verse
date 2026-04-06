package com.datn.backend.service.public_api;

import com.datn.backend.dto.public_api.request.ReadingHistoryRequest;
import java.util.List;

public interface PublicUserActionService {
    void updateReadingHistory(ReadingHistoryRequest request, Integer userId);
    void syncReadingHistory(List<ReadingHistoryRequest> requests, Integer userId);
}
