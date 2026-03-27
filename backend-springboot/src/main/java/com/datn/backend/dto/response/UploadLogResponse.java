package com.datn.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadLogResponse {
    private Integer id;
    private String status;
    private Integer uploaderId;
    private String uploaderName;
    private Integer comicId;
    private String comicTitle;
    private Integer chapterId;
    private String chapterTitle;
    private LocalDateTime reviewAt;
    private Integer reviewerId;
    private LocalDateTime createdAt;
}
