package com.datn.backend.controller.public_api;

import com.datn.backend.dto.public_api.request.ReadingHistoryRequest;
import com.datn.backend.dto.public_api.response.ReadingHistorySyncDTO;
import com.datn.backend.dto.response.ApiResponse;
import com.datn.backend.security.services.UserDetailsImpl;
import com.datn.backend.service.public_api.PublicUserActionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/reading-history")
@RequiredArgsConstructor
public class PublicUserActionController {

    private final PublicUserActionService publicUserActionService;

    // Android client expects a Completable for this, so returning 200 OK without body is fine
    @PostMapping
    public ResponseEntity<Void> updateReadingHistory(@Valid @RequestBody ReadingHistoryRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl)) {
            return ResponseEntity.status(401).build(); // Unauthorized if not logged in
        }
        
        Integer userId = ((UserDetailsImpl) authentication.getPrincipal()).getId();
        publicUserActionService.updateReadingHistory(request, userId);
        
        return ResponseEntity.ok().build();
    }

    @PostMapping("/sync")
    public ResponseEntity<Void> syncReadingHistory(@RequestBody java.util.List<ReadingHistoryRequest> requests) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl)) {
            return ResponseEntity.status(401).build(); // Unauthorized if not logged in
        }
        
        Integer userId = ((UserDetailsImpl) authentication.getPrincipal()).getId();
        publicUserActionService.syncReadingHistory(requests, userId);
        
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReadingHistorySyncDTO>>> getReadingHistory() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl)) {
            return ResponseEntity.status(401).build();
        }

        Integer userId = ((UserDetailsImpl) authentication.getPrincipal()).getId();
        return ResponseEntity.ok(ApiResponse.success(publicUserActionService.getReadingHistory(userId)));
    }
}
