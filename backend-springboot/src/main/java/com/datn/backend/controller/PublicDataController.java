package com.datn.backend.controller;

import com.datn.backend.dto.response.ApiResponse;
import com.datn.backend.dto.response.AuthorResponse;
import com.datn.backend.service.AdminDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Public data endpoints - cho phép Uploader/User đọc master data (authors, genres, ...)
 */
@RestController
@RequestMapping("/api/data")
@RequiredArgsConstructor
public class PublicDataController {

    private final AdminDataService adminDataService;

    @GetMapping("/authors")
    public ApiResponse<List<AuthorResponse>> getAllAuthors() {
        return ApiResponse.success(adminDataService.getAllAuthors());
    }
}
