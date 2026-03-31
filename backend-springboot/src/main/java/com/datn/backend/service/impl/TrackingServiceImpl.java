package com.datn.backend.service.impl;

import com.datn.backend.service.TrackingService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrackingServiceImpl implements TrackingService {

    private final StringRedisTemplate redisTemplate;

    @Override
    public void trackChapterView(Integer comicId, Integer chapterId, HttpServletRequest request) {
        String identifier = getClientIp(request);
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            identifier = auth.getName();
        }

        try {
            // Generate unique key for anti-spam: user/IP can only increase view for a chapter once per 24 hours
            String viewKey = String.format("viewed:comic:%d:chapter:%d:user:%s", comicId, chapterId, identifier);

            // SETNX with 24 hours TTL
            Boolean isNewView = redisTemplate.opsForValue().setIfAbsent(viewKey, "1", 24, TimeUnit.HOURS);

            if (Boolean.TRUE.equals(isNewView)) {
                // Anti-spam passed, tracking a new valid view
                log.info("New view tracked for comic {} chapter {} by {}", comicId, chapterId, identifier);
                
                // Buffer the view count in Redis
                String countKey = "view_count:comic:" + comicId;
                redisTemplate.opsForValue().increment(countKey);
            } else {
                log.debug("View ignored (Spam protection) for comic {} chapter {} by {}", comicId, chapterId, identifier);
            }
        } catch (Exception e) {
            log.error("Redis error while tracking view for comic {} chapter {}: {}", comicId, chapterId, e.getMessage());
            // Optionally, fallback logic here. For now we just catch to prevent 500 error.
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String remoteAddr = "";
        if (request != null) {
            remoteAddr = request.getHeader("X-FORWARDED-FOR");
            if (remoteAddr == null || "".equals(remoteAddr)) {
                remoteAddr = request.getRemoteAddr();
            }
        }
        return remoteAddr;
    }
}
