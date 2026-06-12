package com.datn.backend.dto.public_api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteDTO {
    private Integer comicId;
    private String slug;
    private String title;
    private String thumbnailUrl;
    private String contentType;
    private Long addedAtMillis;
}
