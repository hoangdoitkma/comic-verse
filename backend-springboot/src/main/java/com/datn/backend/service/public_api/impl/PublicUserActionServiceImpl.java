package com.datn.backend.service.public_api.impl;

import com.datn.backend.dto.public_api.request.ReadingHistoryRequest;
import com.datn.backend.dto.public_api.response.ReadingHistorySyncDTO;
import com.datn.backend.entity.Chapter;
import com.datn.backend.entity.Comic;
import com.datn.backend.entity.ReadingHistory;
import com.datn.backend.entity.User;
import com.datn.backend.repository.ChapterRepository;
import com.datn.backend.repository.ComicRepository;
import com.datn.backend.repository.ReadingHistoryRepository;
import com.datn.backend.repository.UserRepository;
import com.datn.backend.service.public_api.PublicUserActionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PublicUserActionServiceImpl implements PublicUserActionService {

    private final ReadingHistoryRepository readingHistoryRepository;
    private final ComicRepository comicRepository;
    private final ChapterRepository chapterRepository;
    private final UserRepository userRepository;

    @Override
    public void updateReadingHistory(ReadingHistoryRequest request, Integer userId) {
        if (userId == null) return;

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Comic comic = comicRepository.findById(request.getComicId())
                .orElseThrow(() -> new RuntimeException("Comic not found"));
        Chapter chapter = chapterRepository.findById(request.getChapterId())
                .orElseThrow(() -> new RuntimeException("Chapter not found"));

        Optional<ReadingHistory> existingOpt = readingHistoryRepository.findByUserIdAndComicId(userId, comic.getId());

        ReadingHistory history;
        if (existingOpt.isPresent()) {
            history = existingOpt.get();
            history.setChapter(chapter);
            history.setLastPage(request.getLastPage() != null ? request.getLastPage() : 1);
        } else {
            history = ReadingHistory.builder()
                    .user(user)
                    .comic(comic)
                    .chapter(chapter)
                    .lastPage(request.getLastPage() != null ? request.getLastPage() : 1)
                    .build();
        }

        readingHistoryRepository.save(history);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void syncReadingHistory(List<ReadingHistoryRequest> requests, Integer userId) {
        if (userId == null || requests == null || requests.isEmpty()) return;
        for (ReadingHistoryRequest request : requests) {
            try {
                updateReadingHistory(request, userId);
            } catch (Exception e) {
                // Ignore errors for individual items to allow the rest to sync
                System.err.println("Error syncing history for comicId: " + request.getComicId() + " - " + e.getMessage());
            }
        }
    }

    @Override
    public List<ReadingHistorySyncDTO> getReadingHistory(Integer userId) {
        if (userId == null) return List.of();

        return readingHistoryRepository.findByUserIdOrderByUpdatedAtDesc(userId).stream()
                .filter(history -> history.getComic() != null && history.getChapter() != null)
                .map(this::mapToSyncDTO)
                .collect(Collectors.toList());
    }

    private ReadingHistorySyncDTO mapToSyncDTO(ReadingHistory history) {
        Comic comic = history.getComic();
        Chapter chapter = history.getChapter();

        return ReadingHistorySyncDTO.builder()
                .comicId(comic.getId())
                .slug(comic.getSlug())
                .title(comic.getTitle())
                .thumbnailUrl(comic.getThumbnailUrl())
                .authorName(comic.getAuthor() != null ? comic.getAuthor().getName() : null)
                .viewCount(comic.getViewCount() != null ? comic.getViewCount().longValue() : 0L)
                .contentType(comic.getContentType() != null ? comic.getContentType().name() : null)
                .chapterId(chapter.getId())
                .chapterTitle(chapter.getTitle())
                .lastPage(history.getLastPage() != null ? history.getLastPage() : 0)
                .updatedAtMillis(history.getUpdatedAt() != null
                        ? history.getUpdatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                        : 0L)
                .build();
    }
}
