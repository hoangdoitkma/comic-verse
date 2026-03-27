package com.datn.backend.dto.request;

import com.datn.backend.entity.enums.UserStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserStatusRequest {
    @NotNull(message = "Status is required")
    private UserStatus status;
}
