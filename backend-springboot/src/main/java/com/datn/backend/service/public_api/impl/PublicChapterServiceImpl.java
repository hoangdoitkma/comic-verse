package com.datn.backend.service.public_api.impl;

import com.datn.backend.dto.public_api.response.ChapterDetailDTO;
import com.datn.backend.dto.public_api.response.ChapterItemDTO;
import com.datn.backend.entity.Chapter;
import com.datn.backend.entity.ChapterPage;
import com.datn.backend.entity.Comic;
import com.datn.backend.repository.ChapterRepository;
import com.datn.backend.repository.ComicRepository;
import com.datn.backend.service.public_api.PublicChapterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PublicChapterServiceImpl implements PublicChapterService {

    private final ChapterRepository chapterRepository;
    private final ComicRepository comicRepository;

    @Override
    public List<ChapterItemDTO> getChaptersByComicSlug(String slug) {
        Comic comic = comicRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Comic not found"));

        return chapterRepository.findByComicIdOrderBySortOrderAsc(comic.getId())
                .stream()
                .map(this::mapToItemDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ChapterDetailDTO getChapterContent(Integer chapterId) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new RuntimeException("Chapter not found"));

        List<String> pages = chapter.getChapterPages().stream()
                .sorted(Comparator.comparingInt(ChapterPage::getPageNumber))
                .map(ChapterPage::getImageUrl)
                .collect(Collectors.toList());

        return ChapterDetailDTO.builder()
                .id(chapter.getId())
                .chapterNumber(chapter.getChapterNumber())
                .title(chapter.getTitle())
                .pages(pages)
                .content(chapter.getContent())
                .build();
    }

    private ChapterItemDTO mapToItemDTO(Chapter chapter) {
        return ChapterItemDTO.builder()
                .id(chapter.getId())
                .chapterNumber(chapter.getChapterNumber())
                .title(chapter.getTitle())
                .accessType(chapter.getAccessType() != null ? chapter.getAccessType().name() : null)
                .viewCount(chapter.getViewCount())
                .createdAt(chapter.getCreatedAt())
                .build();
    }
}
