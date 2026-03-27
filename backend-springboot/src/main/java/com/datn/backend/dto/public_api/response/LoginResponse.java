package com.datn.backend.dto.public_api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    @Builder.Default
    private String type = "Bearer";
    private Integer id;
    private String email;
    private List<String> roles;
    private String displayName;
    private String avatarUrl;
}
