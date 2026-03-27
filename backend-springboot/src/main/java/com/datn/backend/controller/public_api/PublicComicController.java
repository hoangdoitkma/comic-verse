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

    @GetMapping
    public ResponseEntity<ApiResponse<List<ComicDTO>>> getComics(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit) {
        Page<ComicDTO> comicsPage = publicComicService.getComics(page, limit);
        return ResponseEntity.ok(ApiResponse.success(comicsPage.getContent()));
    }

    @GetMapping("/home")
    public ResponseEntity<ApiResponse<HomeDataResponse>> getHomeContent() {
        return ResponseEntity.ok(ApiResponse.success(publicComicService.getHomeContent()));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<ComicDetailDTO>> getComicDetail(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.success(publicComicService.getComicDetail(slug)));
    }

    @GetMapping("/{slug}/chapters")
    public ResponseEntity<ApiResponse<List<ChapterItemDTO>>> getChapters(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.success(publicChapterService.getChaptersByComicSlug(slug)));
    }

    @PostMapping("/reading-history-info")
    public ResponseEntity<ApiResponse<List<ReadingHistoryInfoDTO>>> getReadingHistoryInfo(
            @RequestBody ReadingHistoryInfoRequest request) {
        Integer userId = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
            userId = ((UserDetailsImpl) authentication.getPrincipal()).getId();
        }
        
        List<ReadingHistoryInfoDTO> response = publicComicService.getReadingHistoryInfo(request.getComicIds(), userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
