package com.datn.backend.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadActivityDataDto {
    private String name; // e.g. "T2"
    private long comics;
    private long chapters;
}
