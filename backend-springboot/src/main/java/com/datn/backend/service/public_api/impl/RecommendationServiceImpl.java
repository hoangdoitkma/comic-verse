package com.datn.backend.service.public_api.impl;

import com.datn.backend.dto.public_api.response.ComicDTO;
import com.datn.backend.entity.Comic;
import com.datn.backend.entity.ComicGenre;
import com.datn.backend.entity.ReadingHistory;
import com.datn.backend.entity.enums.ContentType;
import com.datn.backend.repository.ComicGenreRepository;
import com.datn.backend.repository.ComicRepository;
import com.datn.backend.repository.ChapterRepository;
import com.datn.backend.repository.ReadingHistoryRepository;
import com.datn.backend.service.public_api.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationServiceImpl implements RecommendationService {

    private final ReadingHistoryRepository readingHistoryRepository;
    private final ComicGenreRepository comicGenreRepository;
    private final ComicRepository comicRepository;
    private final ChapterRepository chapterRepository;

    private static final int MAX_RECOMMENDATIONS = 10;

    @Override
    public List<ComicDTO> getRecommendedComics(Integer userId, ContentType type) {
        if (userId == null) {
            return getFallbackRecommendations(type);
        }

        try {
            // 1. Lấy danh sách comic đã đọc
            List<ReadingHistory> histories = readingHistoryRepository.findByUserId(userId);
            if (histories.isEmpty()) {
                return getFallbackRecommendations(type);
            }

            List<Integer> readComicIds = histories.stream()
                    .map(h -> h.getComic().getId())
                    .distinct()
                    .collect(Collectors.toList());

            // 2. Tìm tất cả genre từ truyện đã đọc
            List<ComicGenre> readComicGenres = comicGenreRepository.findByComicIdIn(readComicIds);
            if (readComicGenres.isEmpty()) {
                return getFallbackRecommendations(type);
            }

            // Đếm frequency mỗi genre (genre nào xuất hiện nhiều → user thích hơn)
            Map<Integer, Long> genreFrequency = readComicGenres.stream()
                    .collect(Collectors.groupingBy(
                            cg -> cg.getGenre().getId(),
                            Collectors.counting()
                    ));

            List<Integer> favoriteGenreIds = new ArrayList<>(genreFrequency.keySet());

            // 3. Tìm tất cả truyện thuộc các genre yêu thích
            List<ComicGenre> candidateComicGenres = comicGenreRepository.findByGenreIdIn(favoriteGenreIds);

            // 4. Lọc bỏ truyện đã đọc, tính điểm cho truyện còn lại
            Set<Integer> readComicIdSet = new HashSet<>(readComicIds);
            Map<Integer, Double> comicScores = new HashMap<>();

            for (ComicGenre cg : candidateComicGenres) {
                Integer comicId = cg.getComic().getId();
                if (readComicIdSet.contains(comicId)) continue; // Bỏ qua truyện đã đọc

                Integer genreId = cg.getGenre().getId();
                // Score = tổng frequency của genre chung (genre user đọc nhiều → weight cao hơn)
                double genreWeight = genreFrequency.getOrDefault(genreId, 1L);
                comicScores.merge(comicId, genreWeight, Double::sum);
            }

            if (comicScores.isEmpty()) {
                return getFallbackRecommendations(type);
            }

            // 5. Sắp xếp theo điểm genre, lấy top candidates
            List<Integer> topCandidateIds = comicScores.entrySet().stream()
                    .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                    .limit(MAX_RECOMMENDATIONS * 2) // Lấy dư để lọc theo type
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            // 6. Fetch comics từ DB
            List<Comic> candidateComics = comicRepository.findByIdInAndIsDeletedFalse(topCandidateIds);

            // 7. Lọc theo ContentType nếu có
            if (type != null) {
                candidateComics = candidateComics.stream()
                        .filter(c -> type.equals(c.getContentType()))
                        .collect(Collectors.toList());
            }

            // 8. Sắp xếp lại: ưu tiên score cao → view count cao
            candidateComics.sort((a, b) -> {
                double scoreA = comicScores.getOrDefault(a.getId(), 0.0);
                double scoreB = comicScores.getOrDefault(b.getId(), 0.0);
                int cmp = Double.compare(scoreB, scoreA); // Score cao trước
                if (cmp != 0) return cmp;
                // Nếu score bằng → ưu tiên view count
                int viewA = a.getViewCount() != null ? a.getViewCount() : 0;
                int viewB = b.getViewCount() != null ? b.getViewCount() : 0;
                return Integer.compare(viewB, viewA);
            });

            // 9. Lấy top N và map sang DTO
            List<ComicDTO> result = candidateComics.stream()
                    .limit(MAX_RECOMMENDATIONS)
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());

            log.info("Generated {} recommendations for userId={}", result.size(), userId);
            return result;

        } catch (Exception e) {
            log.error("Error generating recommendations for userId={}: {}", userId, e.getMessage());
            return getFallbackRecommendations(type);
        }
    }

    @Override
    public List<ComicDTO> getSimilarComics(String slug, Integer userId) {
        try {
            Optional<Comic> optComic = comicRepository.findBySlugAndIsDeletedFalse(slug);
            if (optComic.isEmpty()) {
                return Collections.emptyList();
            }
            Comic currentComic = optComic.get();
            Integer currentComicId = currentComic.getId();
            Integer currentAuthorId = currentComic.getAuthor() != null ? currentComic.getAuthor().getId() : null;
            ContentType contentType = currentComic.getContentType();
            
            // Get genres of the current comic
            List<ComicGenre> currentComicGenres = comicGenreRepository.findByComicId(currentComicId);
            List<Integer> currentGenreIds = currentComicGenres.stream()
                    .map(cg -> cg.getGenre().getId())
                    .collect(Collectors.toList());

            // Build user preferences
            Map<Integer, Long> userGenreFrequency = new HashMap<>();
            Map<Integer, Long> userAuthorFrequency = new HashMap<>();
            
            if (userId != null) {
                List<ReadingHistory> histories = readingHistoryRepository.findByUserId(userId);
                List<Integer> readComicIds = histories.stream()
                        .map(h -> h.getComic().getId())
                        .distinct()
                        .collect(Collectors.toList());
                
                if (!readComicIds.isEmpty()) {
                    List<ComicGenre> readComicGenres = comicGenreRepository.findByComicIdIn(readComicIds);
                    userGenreFrequency = readComicGenres.stream()
                            .collect(Collectors.groupingBy(cg -> cg.getGenre().getId(), Collectors.counting()));
                    
                    histories.stream()
                            .filter(h -> h.getComic().getAuthor() != null)
                            .forEach(h -> userAuthorFrequency.merge(h.getComic().getAuthor().getId(), 1L, Long::sum));
                }
            }

            Set<Integer> candidateIds = new HashSet<>();
            
            // Candidates from same author
            if (currentAuthorId != null) {
                List<Comic> sameAuthorComics = comicRepository.findByAuthorIdAndIsDeletedFalse(currentAuthorId);
                sameAuthorComics.forEach(c -> candidateIds.add(c.getId()));
            }
            
            // Candidates from same genres
            if (!currentGenreIds.isEmpty()) {
                List<ComicGenre> sameGenreComics = comicGenreRepository.findByGenreIdIn(currentGenreIds);
                sameGenreComics.forEach(cg -> candidateIds.add(cg.getComic().getId()));
            }
            
            // Candidates from user's favorite genres/authors
            if (!userGenreFrequency.isEmpty()) {
                List<Integer> favGenreIds = new ArrayList<>(userGenreFrequency.keySet());
                comicGenreRepository.findByGenreIdIn(favGenreIds)
                        .forEach(cg -> candidateIds.add(cg.getComic().getId()));
            }
            
            candidateIds.remove(currentComicId);
            
            if (candidateIds.isEmpty()) {
                return getFallbackRecommendations(contentType);
            }

            // Fetch candidates
            List<Comic> candidates = comicRepository.findByIdInAndIsDeletedFalse(new ArrayList<>(candidateIds));
            Map<Integer, Double> comicScores = new HashMap<>();
            
            for (Comic candidate : candidates) {
                if (contentType != null && !contentType.equals(candidate.getContentType())) {
                    continue; 
                }
                
                double score = 0;
                Integer candAuthorId = candidate.getAuthor() != null ? candidate.getAuthor().getId() : null;
                
                // Rule 1: Same author
                if (candAuthorId != null && candAuthorId.equals(currentAuthorId)) {
                    score += 10.0;
                }
                
                // Rule 2: Shared genres
                List<Integer> candGenreIds = candidate.getComicGenres().stream()
                        .map(cg -> cg.getGenre().getId())
                        .collect(Collectors.toList());
                long sharedGenresCount = candGenreIds.stream().filter(currentGenreIds::contains).count();
                score += (sharedGenresCount * 5.0);
                
                // Rule 3: User preference
                if (userId != null) {
                    if (candAuthorId != null && userAuthorFrequency.containsKey(candAuthorId)) {
                        score += (userAuthorFrequency.get(candAuthorId) * 5.0);
                    }
                    for (Integer candGenreId : candGenreIds) {
                        if (userGenreFrequency.containsKey(candGenreId)) {
                            score += (userGenreFrequency.get(candGenreId) * 2.0);
                        }
                    }
                }
                
                if (score > 0) {
                    comicScores.put(candidate.getId(), score);
                }
            }
            
            if (comicScores.isEmpty()) {
                return getFallbackRecommendations(contentType);
            }

            // Sort and return top 10
            return candidates.stream()
                    .filter(c -> comicScores.containsKey(c.getId()))
                    .sorted((a, b) -> {
                        double scoreA = comicScores.getOrDefault(a.getId(), 0.0);
                        double scoreB = comicScores.getOrDefault(b.getId(), 0.0);
                        int cmp = Double.compare(scoreB, scoreA); 
                        if (cmp != 0) return cmp;
                        int viewA = a.getViewCount() != null ? a.getViewCount() : 0;
                        int viewB = b.getViewCount() != null ? b.getViewCount() : 0;
                        return Integer.compare(viewB, viewA);
                    })
                    .limit(MAX_RECOMMENDATIONS)
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error generating similar comics for slug={}: {}", slug, e.getMessage());
            return getFallbackRecommendations(null);
        }
    }

    /**
     * Fallback: Trả về truyện trending (view count cao nhất) khi chưa có đủ dữ liệu đề xuất
     */
    private List<ComicDTO> getFallbackRecommendations(ContentType type) {
        List<Comic> fallbackComics;
        if (type != null) {
            fallbackComics = comicRepository.findTop5ByContentTypeAndIsDeletedFalseOrderByViewCountDesc(type);
        } else {
            fallbackComics = comicRepository.findTop5ByIsDeletedFalseOrderByViewCountDesc();
        }
        return fallbackComics.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    private ComicDTO mapToDTO(Comic comic) {
        return ComicDTO.builder()
                .id(comic.getId())
                .title(comic.getTitle())
                .slug(comic.getSlug())
                .thumbnailUrl(comic.getThumbnailUrl())
                .viewCount(comic.getViewCount())
                .averageRating(comic.getAverageRating())
                .totalChapters(getTotalChaptures(comic))
                .status(comic.getStatus() != null ? comic.getStatus().name() : null)
                .contentType(comic.getContentType() != null ? comic.getContentType().name() : null)
                .comicFormat(comic.getComicFormat() != null ? comic.getComicFormat().name() : null)
                .accessType(comic.getAccessType() != null ? comic.getAccessType().name() : "FREE")
                .createdAt(comic.getCreatedAt())
                .updatedAt(comic.getUpdatedAt())
                .build();
    }

    private int getTotalChaptures(Comic comic) {
        if (comic.getTotalChapters() != null && comic.getTotalChapters() > 0) {
            return comic.getTotalChapters();
        }
        try {
            Long count = chapterRepository.countByComicId(comic.getId());
            return count != null ? count.intValue() : 0;
        } catch (Exception e) {
            return 0;
        }
    }
}
