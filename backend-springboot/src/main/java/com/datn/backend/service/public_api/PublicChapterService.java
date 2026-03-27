package com.datn.backend.service.public_api;

import com.datn.backend.dto.public_api.response.ChapterDetailDTO;
import com.datn.backend.dto.public_api.response.ChapterItemDTO;

import java.util.List;

public interface PublicChapterService {
    List<ChapterItemDTO> getChaptersByComicSlug(String slug);
    ChapterDetailDTO getChapterContent(Integer chapterId);
}
