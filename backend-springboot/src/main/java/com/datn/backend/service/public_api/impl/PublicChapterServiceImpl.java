package com.datn.backend.service.public_api.impl;

import com.datn.backend.dto.public_api.response.ChapterDetailDTO;
import com.datn.backend.dto.public_api.response.ChapterItemDTO;
import com.datn.backend.entity.Chapter;
import com.datn.backend.entity.ChapterPage;
import com.datn.backend.entity.Comic;
import com.datn.backend.repository.ChapterRepository;
import com.datn.backend.repository.ComicRepository;
import com.datn.backend.repository.UserRepository;
import com.datn.backend.repository.SubscriptionRepository;
import com.datn.backend.entity.enums.AccessType;
import com.datn.backend.entity.enums.SubscriptionStatus;
import com.datn.backend.service.public_api.PublicChapterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PublicChapterServiceImpl implements PublicChapterService {

    private final ChapterRepository chapterRepository;
    private final ComicRepository comicRepository;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Override
    public List<ChapterItemDTO> getChaptersByComicSlug(String slug) {
        Comic comic = comicRepository.findBySlugAndIsDeletedFalse(slug)
                .orElseThrow(() -> new RuntimeException("Comic not found"));

        return chapterRepository.findByComicIdOrderBySortOrderAsc(comic.getId())
                .stream()
                .map(this::mapToItemDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ChapterItemDTO> getChaptersByComicId(Integer comicId) {
        Comic comic = comicRepository.findById(comicId)
                .orElseThrow(() -> new com.datn.backend.exception.ResourceNotFoundException("Comic", "id", comicId.toString()));
        if (Boolean.TRUE.equals(comic.getIsDeleted())) {
            throw new com.datn.backend.exception.ResourceNotFoundException("Comic", "id", comicId.toString());
        }

        return chapterRepository.findByComicIdOrderBySortOrderAsc(comicId)
                .stream()
                .map(this::mapToItemDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ChapterDetailDTO getChapterContent(Integer chapterId) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new com.datn.backend.exception.ResourceNotFoundException("Chapter", "id", chapterId.toString()));

        if (chapter.getComic() != null && Boolean.TRUE.equals(chapter.getComic().getIsDeleted())) {
            throw new com.datn.backend.exception.ResourceNotFoundException("Chapter", "id", chapterId.toString());
        }

        // Kiểm tra quyền VIP
        Comic comic = chapter.getComic();
        if (AccessType.VIP.equals(chapter.getAccessType()) || (comic != null && AccessType.VIP.equals(comic.getAccessType()))) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
                throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "VIP_REQUIRED_ANONYMOUS");
            }
            String email = auth.getName();
            com.datn.backend.entity.User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "VIP_REQUIRED_USER_NOT_FOUND"));

            List<com.datn.backend.entity.Subscription> activeSubs = subscriptionRepository.findByUserIdAndStatus(user.getId(), SubscriptionStatus.ACTIVE);
            boolean isVip = false;
            LocalDateTime now = LocalDateTime.now();
            for (com.datn.backend.entity.Subscription sub : activeSubs) {
                if (sub.getEndDate() == null || sub.getEndDate().isAfter(now)) {
                    isVip = true;
                    break;
                }
            }
            if (!isVip) {
                throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "VIP_REQUIRED_SUBSCRIPTION_EXPIRED");
            }
        }

        List<String> pages = chapter.getChapterPages().stream()
                .sorted(Comparator.comparingInt(ChapterPage::getPageNumber))
                .map(ChapterPage::getImageUrl)
                .collect(Collectors.toList());

        List<Chapter> allChapters = chapterRepository.findByComicIdOrderBySortOrderAsc(comic.getId());
        
        Integer nextChapterId = null;
        Integer prevChapterId = null;
        
        for (int i = 0; i < allChapters.size(); i++) {
            if (allChapters.get(i).getId().equals(chapterId)) {
                if (i > 0) prevChapterId = allChapters.get(i - 1).getId();
                if (i < allChapters.size() - 1) nextChapterId = allChapters.get(i + 1).getId();
                break;
            }
        }

        return ChapterDetailDTO.builder()
                .id(chapter.getId())
                .chapterNumber(chapter.getChapterNumber())
                .title(chapter.getTitle())
                .pages(pages)
                .content(chapter.getContent())
                .nextChapterId(nextChapterId)
                .prevChapterId(prevChapterId)
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
