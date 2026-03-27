package com.datn.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GenreRequest {
    @NotBlank(message = "Name is required")
    private String name;
    private String description;
}
