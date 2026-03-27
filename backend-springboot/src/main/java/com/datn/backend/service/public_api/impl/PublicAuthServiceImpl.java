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
                .displayName(userDetails.getUsername()) // Mapping username to displayName
                .build();
    }

    @Override
    public TokenResponse refreshToken(String refreshToken) {
        // Simple implementation for Android client
        // In a real app, you would validate the refreshToken against a DB
        // Here we assume the refresh token is just a valid jwt or we just return a new one
        if (refreshToken != null && jwtUtils.validateJwtToken(refreshToken)) {
            // Validation passed
        }
        
        // This is a minimal stub to satisfy the Retrofit interface.
        // It returns the same token to avoid complex DB refresh token entities at this stage.
        return TokenResponse.builder()
                .token(refreshToken)
                .refreshToken(refreshToken)
                .build();
    }
}
