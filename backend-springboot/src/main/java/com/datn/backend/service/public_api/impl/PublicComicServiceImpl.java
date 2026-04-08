package com.datn.backend.service.public_api.impl;

import com.datn.backend.dto.public_api.response.ComicDTO;
import com.datn.backend.dto.public_api.response.ComicDetailDTO;
import com.datn.backend.dto.public_api.response.HomeDataResponse;
import com.datn.backend.dto.public_api.response.ReadingHistoryInfoDTO;
import com.datn.backend.entity.Comic;
import com.datn.backend.entity.ReadingHistory;
import com.datn.backend.repository.ComicRepository;
import com.datn.backend.repository.ReadingHistoryRepository;
import com.datn.backend.service.public_api.PublicComicService;
import com.datn.backend.service.public_api.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PublicComicServiceImpl implements PublicComicService {

    private final ComicRepository comicRepository;
    private final ReadingHistoryRepository readingHistoryRepository;
    private final com.datn.backend.repository.ChapterRepository chapterRepository;
    private final RecommendationService recommendationService;

    @Override
    public Page<ComicDTO> getComics(int page, int limit) {
        Pageable pageable = PageRequest.of(page, limit);
        return comicRepository.findAll(pageable).map(this::mapToDTO);
    }

    @Override
    public ComicDetailDTO getComicDetail(String slug) {
        Comic comic = comicRepository.findBySlugAndIsDeletedFalse(slug)
                .orElseThrow(() -> new com.datn.backend.exception.ResourceNotFoundException("Comic", "slug", slug));

        ComicDetailDTO dto = new ComicDetailDTO();
        // Mapping basic fields
        dto.setId(comic.getId());
        dto.setTitle(comic.getTitle());
        dto.setSlug(comic.getSlug());
        dto.setThumbnailUrl(comic.getThumbnailUrl());
        dto.setViewCount(comic.getViewCount());
        dto.setAverageRating(comic.getAverageRating());
        dto.setTotalChapters(comic.getTotalChapters());
        dto.setStatus(comic.getStatus() != null ? comic.getStatus().name() : null);
        dto.setContentType(comic.getContentType() != null ? comic.getContentType().name() : null);
        dto.setComicFormat(comic.getComicFormat() != null ? comic.getComicFormat().name() : null);
        dto.setAccessType(comic.getAccessType() != null ? comic.getAccessType().name() : "FREE");
        dto.setCreatedAt(comic.getCreatedAt());
        dto.setUpdatedAt(comic.getUpdatedAt());

        // Mapping detail fields
        dto.setSynopsis(comic.getSynopsis());
        if (comic.getAuthor() != null) {
            dto.setAuthorName(comic.getAuthor().getName());
        }
        if (comic.getAgeRating() != null) {
            dto.setAgeRating(comic.getAgeRating().getLabel());
        }
        // Assuming genres mapping is complex or missing, we skip it for now or set empty
        dto.setGenres(List.of());

        return dto;
    }

    @Override
    public HomeDataResponse getHomeContent(com.datn.backend.entity.enums.ContentType type, Integer userId) {
        List<ComicDTO> topTrending;
        List<ComicDTO> recentlyUpdated;
        List<ComicDTO> newComics;
        
        if (type == null) {
            topTrending = comicRepository.findTop5ByIsDeletedFalseOrderByViewCountDesc()
                    .stream().map(this::mapToDTO).collect(Collectors.toList());
            recentlyUpdated = comicRepository.findTop15ByIsDeletedFalseOrderByUpdatedAtDesc()
                    .stream().map(this::mapToDTO).collect(Collectors.toList());
            newComics = comicRepository.findTop10ByIsDeletedFalseOrderByCreatedAtDesc()
                    .stream().map(this::mapToDTO).collect(Collectors.toList());
        } else {
            topTrending = comicRepository.findTop5ByContentTypeAndIsDeletedFalseOrderByViewCountDesc(type)
                    .stream().map(this::mapToDTO).collect(Collectors.toList());
            recentlyUpdated = comicRepository.findTop15ByContentTypeAndIsDeletedFalseOrderByUpdatedAtDesc(type)
                    .stream().map(this::mapToDTO).collect(Collectors.toList());
            newComics = comicRepository.findTop10ByContentTypeAndIsDeletedFalseOrderByCreatedAtDesc(type)
                    .stream().map(this::mapToDTO).collect(Collectors.toList());
        }

        // Đề xuất dựa trên lịch sử đọc (hoặc fallback trending nếu chưa login)
        List<ComicDTO> recommended = recommendationService.getRecommendedComics(userId, type);

        return HomeDataResponse.builder()
                .topTrending(topTrending)
                .recentlyUpdated(recentlyUpdated)
                .newComics(newComics)
                .recommended(recommended)
                .build();
    }

    @Override
    public List<ReadingHistoryInfoDTO> getReadingHistoryInfo(List<Integer> comicIds, Integer userId) {
        if (comicIds == null || comicIds.isEmpty()) {
            return List.of();
        }
        List<Comic> comics = comicRepository.findByIdInAndIsDeletedFalse(comicIds);
        return comics.stream().map(comic -> {
            ReadingHistoryInfoDTO dto = ReadingHistoryInfoDTO.builder()
                    .comicId(comic.getId())
                    .title(comic.getTitle())
                    .thumbnailUrl(comic.getThumbnailUrl())
                    .build();

            if (userId != null) {
                readingHistoryRepository.findByUserIdAndComicId(userId, comic.getId())
                        .ifPresent(history -> {
                            if (history.getChapter() != null) {
                                dto.setLastReadChapterId(history.getChapter().getId());
                                dto.setLastReadChapterNumber(history.getChapter().getChapterNumber());
                                dto.setLastReadChapterTitle(history.getChapter().getTitle());
                            }
                        });
            }
            return dto;
        }).collect(Collectors.toList());
    }

    private ComicDTO mapToDTO(Comic comic) {
        return ComicDTO.builder()
                .id(comic.getId())
                .title(comic.getTitle())
                .slug(comic.getSlug())
                .thumbnailUrl(comic.getThumbnailUrl())
                .viewCount(comic.getViewCount())
                .averageRating(comic.getAverageRating())
                .totalChapters(comic.getTotalChapters() != null && comic.getTotalChapters() > 0 ? comic.getTotalChapters() : getChapterCountSafely(comic.getId()))
                .status(comic.getStatus() != null ? comic.getStatus().name() : null)
                .contentType(comic.getContentType() != null ? comic.getContentType().name() : null)
                .comicFormat(comic.getComicFormat() != null ? comic.getComicFormat().name() : null)
                .accessType(comic.getAccessType() != null ? comic.getAccessType().name() : "FREE")
                .createdAt(comic.getCreatedAt())
                .updatedAt(comic.getUpdatedAt())
                .build();
    }

    private int getChapterCountSafely(Integer comicId) {
        try {
            Long count = chapterRepository.countByComicId(comicId);
            return count != null ? count.intValue() : 0;
        } catch (Exception e) {
            return 0;
        }
    }
}
