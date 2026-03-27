package com.datn.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ReviewRequest {
    @NotBlank(message = "Status is required")
    @Pattern(regexp = "^(APPROVED|REJECTED)$", message = "Status must be APPROVED or REJECTED")
    private String status;
    private String reason;
}
