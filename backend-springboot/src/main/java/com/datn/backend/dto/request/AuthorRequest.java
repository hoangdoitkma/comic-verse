package com.datn.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthorRequest {
    @NotBlank(message = "Name is required")
    private String name;
    private String studio;
    private String country;
}
