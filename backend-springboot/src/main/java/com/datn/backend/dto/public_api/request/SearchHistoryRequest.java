package com.datn.backend.dto.public_api.request;

import com.datn.backend.entity.enums.ContentType;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchHistoryRequest {
    @NotBlank(message = "Keyword cannot be blank")
    private String keyword;

    private ContentType type;
}
