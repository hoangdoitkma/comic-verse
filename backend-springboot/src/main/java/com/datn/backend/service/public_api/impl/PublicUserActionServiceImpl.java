package com.datn.backend.service.public_api.impl;

import com.datn.backend.dto.public_api.request.ReadingHistoryRequest;
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

import java.util.Optional;

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
}
