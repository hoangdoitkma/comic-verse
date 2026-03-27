package com.datn.backend.controller;

import com.datn.backend.dto.request.ComicRequest;
import com.datn.backend.dto.response.ComicResponse;
import com.datn.backend.dto.response.ApiResponse;
import com.datn.backend.service.ComicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/uploader/comics")
@PreAuthorize("hasAnyRole('UPLOADER', 'ADMIN')")
public class ComicController {

    @Autowired
    private ComicService comicService;

    @PostMapping
    public ApiResponse<ComicResponse> createComic(@ModelAttribute ComicRequest request,
                                                  @RequestParam("thumbnail") MultipartFile thumbnail) {
        ComicResponse response = comicService.createComic(request, thumbnail);
        return ApiResponse.success(response);
    }

    @GetMapping
    public ApiResponse<List<ComicResponse>> getMyComics() {
        List<ComicResponse> comics = comicService.getComicsByUploader();
        return ApiResponse.success(comics);
    }

    @PutMapping("/{comicId}")
    public ApiResponse<ComicResponse> updateComic(@PathVariable Integer comicId,
                                                  @RequestBody ComicRequest request) {
        ComicResponse response = comicService.updateComic(comicId, request);
        return ApiResponse.success(response);
    }
}
