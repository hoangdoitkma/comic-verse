package com.datn.backend.controller.public_api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.datn.backend.dto.public_api.request.LoginRequest;
import com.datn.backend.dto.public_api.response.LoginResponse;
import com.datn.backend.dto.public_api.response.TokenResponse;
import com.datn.backend.dto.response.ApiResponse;
import com.datn.backend.service.public_api.PublicAuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth") // Mobile app hits "auth/login", config base may vary
@RequiredArgsConstructor
public class PublicAuthController {

    private final PublicAuthService publicAuthService;

    // Login matching exactly Android's BaseResponse<LoginResponse> expectation
    @PostMapping({ "/login", "/public/login" })
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = publicAuthService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Login successfully"));
    }

    // Refresh token matching BaseResponse<TokenResponse> expectation
    @PostMapping({ "/refresh", "/public/refresh" })
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(@RequestBody TokenResponse request) {
        TokenResponse response = publicAuthService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success(response, "Token refreshed"));
    }
}
