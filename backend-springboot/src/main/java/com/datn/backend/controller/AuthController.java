package com.datn.backend.controller;

import com.datn.backend.dto.request.LoginRequest;
import com.datn.backend.dto.request.RegisterRequest;
import com.datn.backend.dto.response.ApiResponse;
import com.datn.backend.dto.response.JwtResponse;
import com.datn.backend.entity.User;
import com.datn.backend.entity.enums.Role;
import com.datn.backend.entity.enums.UserStatus;
import com.datn.backend.repository.UserRepository;
import com.datn.backend.security.jwt.JwtUtils;
import com.datn.backend.security.services.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponse>> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        JwtResponse jwtResponse = JwtResponse.builder()
                .token(jwt)
                .id(userDetails.getId())
                .email(userDetails.getEmail())
                .roles(roles)
                .build();

        return ResponseEntity.ok(ApiResponse.success(jwtResponse, "Login successfully"));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Object>> registerUser(@Valid @RequestBody RegisterRequest signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.error(400, "Error: Email is already in use!"));
        }

        User user = User.builder()
                .email(signUpRequest.getEmail())
                .password(encoder.encode(signUpRequest.getPassword()))
                .displayName(signUpRequest.getDisplayName())
                .role(Role.USER)
                .status(UserStatus.ACTIVE)
                .build();

        userRepository.save(user);

        return ResponseEntity.ok(ApiResponse.success(null, "User registered successfully!"));
    }
}
