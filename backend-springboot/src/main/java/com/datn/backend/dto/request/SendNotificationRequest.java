package com.datn.backend.dto.request;

import com.datn.backend.entity.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendNotificationRequest {
    @NotBlank(message = "Title cannot be blank")
    private String title;

    @NotBlank(message = "Message cannot be blank")
    private String message;

    @NotNull(message = "Type must be specified")
    private NotificationType type;

    @Builder.Default
    private Boolean isBroadcast = false;

    private Integer targetUserId;
}
