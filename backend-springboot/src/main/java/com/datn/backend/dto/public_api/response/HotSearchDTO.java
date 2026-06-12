package com.datn.backend.dto.public_api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotSearchDTO {
    private String keyword;
    private String contentType;
    private Long searchCount;
    private Long lastSearchedAtMillis;
}
