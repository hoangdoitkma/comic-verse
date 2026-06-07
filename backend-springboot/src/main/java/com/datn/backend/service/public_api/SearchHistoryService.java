package com.datn.backend.service.public_api;

import com.datn.backend.dto.public_api.request.SearchHistoryRequest;
import com.datn.backend.dto.public_api.response.HotSearchDTO;
import com.datn.backend.dto.public_api.response.SearchHistoryItemDTO;
import com.datn.backend.entity.enums.ContentType;

import java.util.List;

public interface SearchHistoryService {
    void recordSearch(SearchHistoryRequest request, Integer userId);

    List<SearchHistoryItemDTO> getUserHistory(Integer userId, ContentType type, int limit);

    List<HotSearchDTO> getHotSearches(ContentType type, int limit);

    void deleteUserKeyword(Integer userId, String keyword, ContentType type);

    void deleteUserHistory(Integer userId, ContentType type);
}
