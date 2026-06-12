package com.datn.backend.controller.public_api;

import com.datn.backend.dto.public_api.request.FavoriteRequest;
import com.datn.backend.dto.public_api.response.FavoriteDTO;
import com.datn.backend.dto.response.ApiResponse;
import com.datn.backend.entity.Comic;
import com.datn.backend.entity.Follow;
import com.datn.backend.entity.User;
import com.datn.backend.repository.ComicRepository;
import com.datn.backend.repository.FollowRepository;
import com.datn.backend.repository.UserRepository;
import com.datn.backend.security.services.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class PublicFavoriteController {

    private final FollowRepository followRepository;
    private final ComicRepository comicRepository;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<FavoriteDTO>>> getFavorites() {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        List<FavoriteDTO> favorites = followRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .filter(follow -> follow.getComic() != null && !Boolean.TRUE.equals(follow.getComic().getIsDeleted()))
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(favorites));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> addFavorite(@Valid @RequestBody FavoriteRequest request) {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        addFavoriteBySlug(userId, request.getSlug());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/sync")
    public ResponseEntity<ApiResponse<Void>> syncFavorites(@RequestBody List<FavoriteRequest> requests) {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        if (requests != null) {
            for (FavoriteRequest request : requests) {
                if (request == null || request.getSlug() == null || request.getSlug().isBlank()) {
                    continue;
                }
                try {
                    addFavoriteBySlug(userId, request.getSlug());
                } catch (Exception ignored) {
                    // Keep syncing the rest even if one local favorite is stale.
                }
            }
        }

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/{slug}")
    public ResponseEntity<ApiResponse<Void>> removeFavorite(@PathVariable String slug) {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        comicRepository.findBySlugAndIsDeletedFalse(slug)
                .flatMap(comic -> followRepository.findByUserIdAndComicId(userId, comic.getId()))
                .ifPresent(followRepository::delete);

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private void addFavoriteBySlug(Integer userId, String slug) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Comic comic = comicRepository.findBySlugAndIsDeletedFalse(slug)
                .orElseThrow(() -> new RuntimeException("Comic not found"));

        if (!followRepository.existsByUserIdAndComicId(userId, comic.getId())) {
            followRepository.save(Follow.builder()
                    .user(user)
                    .comic(comic)
                    .build());
        }
    }

    private Integer getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl)) {
            return null;
        }
        return ((UserDetailsImpl) authentication.getPrincipal()).getId();
    }

    private FavoriteDTO mapToDTO(Follow follow) {
        Comic comic = follow.getComic();
        return FavoriteDTO.builder()
                .comicId(comic.getId())
                .slug(comic.getSlug())
                .title(comic.getTitle())
                .thumbnailUrl(comic.getThumbnailUrl())
                .contentType(comic.getContentType() != null ? comic.getContentType().name() : null)
                .addedAtMillis(follow.getCreatedAt() != null
                        ? follow.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        : 0L)
                .build();
    }
}
