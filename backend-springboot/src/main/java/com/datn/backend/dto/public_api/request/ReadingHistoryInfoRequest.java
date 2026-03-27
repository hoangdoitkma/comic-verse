package com.datn.backend.dto.public_api.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReadingHistoryInfoRequest {
    @NotEmpty(message = "Comic IDs list cannot be empty")
    private List<Integer> comicIds;
}
