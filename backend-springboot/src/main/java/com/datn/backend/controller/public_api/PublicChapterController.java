package com.datn.backend.controller.public_api;

import com.datn.backend.dto.public_api.response.ChapterDetailDTO;
import com.datn.backend.dto.response.ApiResponse;
import com.datn.backend.service.public_api.PublicChapterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/chapters")
@RequiredArgsConstructor
public class PublicChapterController {

    private final PublicChapterService publicChapterService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ChapterDetailDTO>> getChapterContent(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(publicChapterService.getChapterContent(id)));
    }
}
