package com.datn.backend.controller.public_api;

import com.datn.backend.dto.request.ViewTrackingRequest;
import com.datn.backend.service.TrackingService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tracking")
@RequiredArgsConstructor
public class TrackingController {

    private final TrackingService trackingService;

    @PostMapping("/view")
    public ResponseEntity<?> trackView(@RequestBody ViewTrackingRequest request, HttpServletRequest httpRequest) {
        trackingService.trackChapterView(request.getComicId(), request.getChapterId(), httpRequest);
        return ResponseEntity.ok().build();
    }
}
