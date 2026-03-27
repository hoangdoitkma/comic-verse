package com.datn.backend.service.public_api;

import com.datn.backend.dto.public_api.request.LoginRequest;
import com.datn.backend.dto.public_api.response.LoginResponse;
import com.datn.backend.dto.public_api.response.TokenResponse;

public interface PublicAuthService {
    LoginResponse login(LoginRequest request);
    TokenResponse refreshToken(String refreshToken);
}
