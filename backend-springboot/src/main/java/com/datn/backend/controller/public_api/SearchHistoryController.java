package com.datn.backend.controller.public_api;

import com.datn.backend.dto.public_api.request.SearchHistoryRequest;
import com.datn.backend.dto.public_api.response.HotSearchDTO;
import com.datn.backend.dto.public_api.response.SearchHistoryItemDTO;
import com.datn.backend.dto.response.ApiResponse;
import com.datn.backend.entity.enums.ContentType;
import com.datn.backend.security.services.UserDetailsImpl;
import com.datn.backend.service.public_api.SearchHistoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/search-history")
@RequiredArgsConstructor
public class SearchHistoryController {

    private final SearchHistoryService searchHistoryService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> recordSearch(@Valid @RequestBody SearchHistoryRequest request) {
        searchHistoryService.recordSearch(request, getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SearchHistoryItemDTO>>> getMySearchHistory(
            @RequestParam(required = false) ContentType type,
            @RequestParam(defaultValue = "10") int limit) {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(ApiResponse.success(searchHistoryService.getUserHistory(userId, type, limit)));
    }

    @GetMapping("/hot")
    public ResponseEntity<ApiResponse<List<HotSearchDTO>>> getHotSearches(
            @RequestParam(required = false) ContentType type,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(ApiResponse.success(searchHistoryService.getHotSearches(type, limit)));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteKeyword(
            @RequestParam String keyword,
            @RequestParam(required = false) ContentType type) {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        searchHistoryService.deleteUserKeyword(userId, keyword, type);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/all")
    public ResponseEntity<ApiResponse<Void>> clearMySearchHistory(
            @RequestParam(required = false) ContentType type) {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        searchHistoryService.deleteUserHistory(userId, type);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private Integer getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl)) {
            return null;
        }
        return ((UserDetailsImpl) authentication.getPrincipal()).getId();
    }
}
