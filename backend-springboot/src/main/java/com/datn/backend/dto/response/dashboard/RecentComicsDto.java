package com.datn.backend.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentComicsDto {
    private Integer id;
    private String title;
    private String thumbnail;
    private String uploaderName;
    private String status;
    private LocalDateTime updatedAt;
}
