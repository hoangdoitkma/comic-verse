package com.datn.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ComicRequest {
    @NotBlank(message = "Title cannot be empty")
    private String title;

    private String slug;

    private String synopsis;

    private Integer authorId;

    private Integer ageRatingId;

    @NotNull(message = "Content type is required")
    private com.datn.backend.entity.enums.ContentType contentType;

    @NotNull(message = "Comic format is required")
    private com.datn.backend.entity.enums.ComicFormat comicFormat;
}
