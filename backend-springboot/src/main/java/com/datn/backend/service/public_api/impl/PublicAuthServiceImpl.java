package com.datn.backend.service.public_api.impl;

import com.datn.backend.dto.public_api.request.LoginRequest;
import com.datn.backend.dto.public_api.response.LoginResponse;
import com.datn.backend.dto.public_api.response.TokenResponse;
import com.datn.backend.security.jwt.JwtUtils;
import com.datn.backend.security.services.UserDetailsImpl;
import com.datn.backend.service.public_api.PublicAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PublicAuthServiceImpl implements PublicAuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @Override
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return LoginResponse.builder()
                .token(jwt)
                .type("Bearer")
                .id(userDetails.getId())
                .email(userDetails.getEmail())
                .roles(roles)
                .displayName(userDetails.getDisplayName()) // Correct mapping
                .build();
    }

    @Override
    public TokenResponse refreshToken(String refreshToken) {
        // Validate the incoming refresh token
        if (refreshToken != null && jwtUtils.validateJwtToken(refreshToken)) {
            // Lấy username (email) từ token cũ đang còn hạn
            String username = jwtUtils.getUserNameFromJwtToken(refreshToken);
            
            // Sinh ra 1 cặp token mới toanh (thời gian sống được refresh về 24h tiếp theo)
            String newToken = jwtUtils.generateTokenFromUsername(username);
            
            return TokenResponse.builder()
                    .token(newToken)
                    .refreshToken(newToken)
                    .build();
        }
        
        throw new RuntimeException("Refresh token is invalid or expired!");
    }
}
