package com.datn.backend.dto.public_api.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteRequest {
    @NotBlank(message = "Comic slug cannot be blank")
    private String slug;
}
