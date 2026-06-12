package com.datn.backend.controller.public_api;

import com.datn.backend.dto.public_api.request.ReadingHistoryInfoRequest;
import com.datn.backend.dto.public_api.response.ComicDTO;
import com.datn.backend.dto.public_api.response.ComicDetailDTO;
import com.datn.backend.dto.public_api.response.ChapterItemDTO;
import com.datn.backend.dto.public_api.response.HomeDataResponse;
import com.datn.backend.dto.public_api.response.ReadingHistoryInfoDTO;
import com.datn.backend.dto.response.ApiResponse;
import com.datn.backend.security.services.UserDetailsImpl;
import com.datn.backend.service.public_api.PublicComicService;
import com.datn.backend.service.public_api.PublicChapterService;
import com.datn.backend.service.public_api.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/comics")
@RequiredArgsConstructor
public class PublicComicController {

    private final PublicComicService publicComicService;
    private final PublicChapterService publicChapterService;
    private final RecommendationService recommendationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ComicDTO>>> getComics(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) com.datn.backend.entity.enums.ContentType type,
            @RequestParam(required = false) com.datn.backend.entity.enums.OriginCountry country,
            @RequestParam(required = false) Integer genreId,
            @RequestParam(required = false) com.datn.backend.entity.enums.ComicStatus status) {
        Page<ComicDTO> comicsPage = publicComicService.searchComics(page, limit, keyword, type, country, genreId, status);
        return ResponseEntity.ok(ApiResponse.success(comicsPage.getContent()));
    }

    @GetMapping("/home")
    public ResponseEntity<ApiResponse<HomeDataResponse>> getHomeContent(
            @RequestParam(required = false) com.datn.backend.entity.enums.ContentType type) {
        // Extract userId từ JWT token (nếu đã login)
        Integer userId = extractUserId();
        return ResponseEntity.ok(ApiResponse.success(publicComicService.getHomeContent(type, userId)));
    }

    @GetMapping("/recommendations")
    public ResponseEntity<ApiResponse<List<ComicDTO>>> getRecommendations(
            @RequestParam(required = false) com.datn.backend.entity.enums.ContentType type) {
        Integer userId = extractUserId();
        List<ComicDTO> recommendations = recommendationService.getRecommendedComics(userId, type);
        return ResponseEntity.ok(ApiResponse.success(recommendations));
    }

    @GetMapping("/{slug}/similar")
    public ResponseEntity<ApiResponse<List<ComicDTO>>> getSimilarComics(@PathVariable String slug) {
        Integer userId = extractUserId();
        List<ComicDTO> similarComics = recommendationService.getSimilarComics(slug, userId);
        return ResponseEntity.ok(ApiResponse.success(similarComics));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<ComicDetailDTO>> getComicDetail(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.success(publicComicService.getComicDetail(slug)));
    }

    @GetMapping("/{slug}/chapters")
    public ResponseEntity<ApiResponse<List<ChapterItemDTO>>> getChapters(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.success(publicChapterService.getChaptersByComicSlug(slug)));
    }

    @GetMapping("/id/{id}/chapters")
    public ResponseEntity<ApiResponse<List<ChapterItemDTO>>> getChaptersById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(publicChapterService.getChaptersByComicId(id)));
    }

    @PostMapping("/reading-history-info")
    public ResponseEntity<ApiResponse<List<ReadingHistoryInfoDTO>>> getReadingHistoryInfo(
            @RequestBody ReadingHistoryInfoRequest request) {
        Integer userId = extractUserId();
        List<ReadingHistoryInfoDTO> response = publicComicService.getReadingHistoryInfo(request.getComicIds(), userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Helper: Extract userId từ SecurityContext (JWT Token).
     * Trả về null nếu user chưa login.
     */
    private Integer extractUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
            return ((UserDetailsImpl) authentication.getPrincipal()).getId();
        }
        return null;
    }
}
