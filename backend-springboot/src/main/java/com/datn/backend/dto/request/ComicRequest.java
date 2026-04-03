package com.datn.backend.dto.request;

import com.datn.backend.entity.enums.PublishStatus;
import com.datn.backend.entity.enums.OriginCountry;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

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

    private com.datn.backend.entity.enums.ComicFormat comicFormat;

    private com.datn.backend.entity.enums.AccessType accessType;

    private PublishStatus publishStatus;

    private OriginCountry originCountry;

    private List<Integer> genreIds;
}
