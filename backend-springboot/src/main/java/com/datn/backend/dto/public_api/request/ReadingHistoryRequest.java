package com.datn.backend.dto.public_api.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReadingHistoryRequest {
    @NotNull(message = "Comic ID cannot be null")
    private Integer comicId;

    @NotNull(message = "Chapter ID cannot be null")
    private Integer chapterId;

    private Integer lastPage;
}
