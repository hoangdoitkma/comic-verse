package com.datn.backend.controller.admin;

import com.datn.backend.dto.request.ComicRequest;
import com.datn.backend.dto.response.ApiResponse;
import com.datn.backend.dto.response.ComicResponse;
import com.datn.backend.service.ComicService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/comics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminComicController {

    private final ComicService comicService;

    @GetMapping
    public ApiResponse<Page<ComicResponse>> getAllComics(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(comicService.getAllComicsForAdmin(page, size));
    }

    @PostMapping
    public ApiResponse<ComicResponse> createComic(@ModelAttribute ComicRequest request,
                                                  @RequestParam("thumbnail") MultipartFile thumbnail) {
        ComicResponse response = comicService.createComic(request, thumbnail);
        return ApiResponse.success(response);
    }

    @PutMapping("/{comicId}")
    public ApiResponse<ComicResponse> updateComic(@PathVariable Integer comicId,
                                                  @RequestBody ComicRequest request) {
        ComicResponse response = comicService.updateComic(comicId, request);
        return ApiResponse.success(response);
    }

    @DeleteMapping("/{comicId}")
    public ApiResponse<Void> deleteComic(@PathVariable Integer comicId) {
        comicService.deleteComic(comicId);
        return ApiResponse.success(null, "Comic moved to recycle bin");
    }

    @PostMapping("/{comicId}/restore")
    public ApiResponse<Void> restoreComic(@PathVariable Integer comicId) {
        comicService.restoreComic(comicId);
        return ApiResponse.success(null, "Comic restored successfully");
    }
}
