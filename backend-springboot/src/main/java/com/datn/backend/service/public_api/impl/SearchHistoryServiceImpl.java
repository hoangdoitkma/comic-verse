package com.datn.backend.service.public_api.impl;

import com.datn.backend.dto.public_api.request.SearchHistoryRequest;
import com.datn.backend.dto.public_api.response.HotSearchDTO;
import com.datn.backend.dto.public_api.response.SearchHistoryItemDTO;
import com.datn.backend.entity.SearchHistory;
import com.datn.backend.entity.User;
import com.datn.backend.entity.enums.ContentType;
import com.datn.backend.repository.SearchHistoryRepository;
import com.datn.backend.repository.UserRepository;
import com.datn.backend.service.public_api.SearchHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchHistoryServiceImpl implements SearchHistoryService {

    private static final int HOT_SEARCH_DAYS = 7;
    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_LIMIT = 30;
    private static final int USER_HISTORY_FETCH_MULTIPLIER = 5;
    private static final int MIN_KEYWORD_LENGTH = 2;
    private static final int MAX_KEYWORD_LENGTH = 255;
    private static final int USER_DUPLICATE_WINDOW_MINUTES = 5;

    private final SearchHistoryRepository searchHistoryRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void recordSearch(SearchHistoryRequest request, Integer userId) {
        String keyword = sanitizeKeyword(request != null ? request.getKeyword() : null);
        String normalizedKeyword = normalizeKeyword(keyword);
        if (normalizedKeyword.length() < MIN_KEYWORD_LENGTH) {
            return;
        }

        ContentType type = request != null ? request.getType() : null;
        User user = null;
        if (userId != null) {
            user = userRepository.findById(userId).orElse(null);
            if (user != null && isRecentDuplicate(userId, normalizedKeyword, type)) {
                return;
            }
        }

        searchHistoryRepository.save(SearchHistory.builder()
                .user(user)
                .keyword(keyword)
                .normalizedKeyword(normalizedKeyword)
                .contentType(type)
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SearchHistoryItemDTO> getUserHistory(Integer userId, ContentType type, int limit) {
        if (userId == null) {
            return List.of();
        }

        int safeLimit = sanitizeLimit(limit);
        List<SearchHistory> raw = searchHistoryRepository.findUserHistory(
                userId,
                type,
                PageRequest.of(0, MAX_LIMIT * USER_HISTORY_FETCH_MULTIPLIER)
        );

        Map<String, SearchHistory> deduped = new LinkedHashMap<>();
        for (SearchHistory item : raw) {
            if (item.getNormalizedKeyword() == null || item.getNormalizedKeyword().isBlank()) {
                continue;
            }
            deduped.putIfAbsent(item.getNormalizedKeyword(), item);
            if (deduped.size() >= safeLimit) {
                break;
            }
        }

        return deduped.values().stream()
                .map(this::mapToHistoryItem)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<HotSearchDTO> getHotSearches(ContentType type, int limit) {
        int safeLimit = sanitizeLimit(limit);
        LocalDateTime since = LocalDateTime.now().minusDays(HOT_SEARCH_DAYS);
        return searchHistoryRepository.findHotSearches(since, type, PageRequest.of(0, safeLimit))
                .stream()
                .map(this::mapToHotSearch)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteUserKeyword(Integer userId, String keyword, ContentType type) {
        if (userId == null) {
            return;
        }
        String normalizedKeyword = normalizeKeyword(keyword);
        if (normalizedKeyword.isBlank()) {
            return;
        }
        searchHistoryRepository.deleteUserKeyword(userId, normalizedKeyword, type);
    }

    @Override
    @Transactional
    public void deleteUserHistory(Integer userId, ContentType type) {
        if (userId == null) {
            return;
        }
        searchHistoryRepository.deleteUserHistory(userId, type);
    }

    private boolean isRecentDuplicate(Integer userId, String normalizedKeyword, ContentType type) {
        return searchHistoryRepository
                .findFirstByUserIdAndNormalizedKeywordAndContentTypeOrderBySearchedAtDesc(userId, normalizedKeyword, type)
                .filter(history -> history.getSearchedAt() != null)
                .map(history -> history.getSearchedAt().isAfter(LocalDateTime.now().minusMinutes(USER_DUPLICATE_WINDOW_MINUTES)))
                .orElse(false);
    }

    private SearchHistoryItemDTO mapToHistoryItem(SearchHistory history) {
        return SearchHistoryItemDTO.builder()
                .keyword(history.getKeyword())
                .contentType(history.getContentType() != null ? history.getContentType().name() : null)
                .searchedAtMillis(toMillis(history.getSearchedAt()))
                .build();
    }

    private HotSearchDTO mapToHotSearch(SearchHistoryRepository.HotSearchProjection projection) {
        return HotSearchDTO.builder()
                .keyword(projection.getKeyword())
                .contentType(projection.getContentType() != null ? projection.getContentType().name() : null)
                .searchCount(projection.getSearchCount() != null ? projection.getSearchCount() : 0L)
                .lastSearchedAtMillis(toMillis(projection.getLastSearchedAt()))
                .build();
    }

    private int sanitizeLimit(int limit) {
        if (limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private String sanitizeKeyword(String keyword) {
        if (keyword == null) {
            return "";
        }
        String sanitized = keyword.trim().replaceAll("\\s+", " ");
        if (sanitized.length() > MAX_KEYWORD_LENGTH) {
            return sanitized.substring(0, MAX_KEYWORD_LENGTH);
        }
        return sanitized;
    }

    private String normalizeKeyword(String keyword) {
        return sanitizeKeyword(keyword).toLowerCase(Locale.ROOT);
    }

    private long toMillis(LocalDateTime dateTime) {
        if (dateTime == null) {
            return 0L;
        }
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}
