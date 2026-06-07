package com.datn.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Reset code cannot be empty")
    @Pattern(regexp = "\\d{6}", message = "Reset code must contain 6 digits")
    private String otp;

    @NotBlank(message = "New password cannot be empty")
    @Size(min = 6, max = 40, message = "Password must be between 6 and 40 characters")
    private String newPassword;
}
