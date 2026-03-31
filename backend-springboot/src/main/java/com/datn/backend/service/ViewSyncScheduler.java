package com.datn.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ViewSyncScheduler {

    private final StringRedisTemplate redisTemplate;
    private final JdbcTemplate jdbcTemplate;

    /**
     * Cron Job: Runs every 1 minute to sync view count from Redis back to MySQL (for testing)
     */
    @Scheduled(cron = "0 * * * * ?")
    @Transactional
    public void syncViewsToDatabase() {
        Set<String> keys = redisTemplate.keys("view_count:comic:*");
        
        if (keys != null && !keys.isEmpty()) {
            log.info("Start syncing {} comic view counts from Redis to MySQL", keys.size());

            for (String key : keys) {
                try {
                    // Get current value and then set back to 0 (or decrement)
                    // Using getAndSet is atomic and prevents losing views that are added concurrently
                    String countStr = redisTemplate.opsForValue().get(key);
                    if (countStr != null) {
                        redisTemplate.delete(key);
                        int viewsToAdd = Integer.parseInt(countStr);
                        if (viewsToAdd > 0) {
                            String[] parts = key.split(":");
                            if (parts.length >= 3) {
                                int comicId = Integer.parseInt(parts[2]);
                                
                                // Safe batch update
                                int updated = jdbcTemplate.update("UPDATE comics SET view_count = COALESCE(view_count, 0) + ? WHERE id = ?", viewsToAdd, comicId);
                                log.info("Synced comic {} with {} new views. Rows affected: {}", comicId, viewsToAdd, updated);
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("Failed to sync view for key: " + key, e);
                }
            }
        }

        // Sync Chapter views
        Set<String> chapterKeys = redisTemplate.keys("view_count:chapter:*");
        if (chapterKeys != null && !chapterKeys.isEmpty()) {
            log.info("Start syncing {} chapter view counts from Redis to MySQL", chapterKeys.size());
            for (String key : chapterKeys) {
                try {
                    String countStr = redisTemplate.opsForValue().get(key);
                    if (countStr != null) {
                        redisTemplate.delete(key);
                        int viewsToAdd = Integer.parseInt(countStr);
                        if (viewsToAdd > 0) {
                            String[] parts = key.split(":");
                            if (parts.length >= 3) {
                                int chapterId = Integer.parseInt(parts[2]);
                                int updated = jdbcTemplate.update("UPDATE chapters SET view_count = COALESCE(view_count, 0) + ? WHERE id = ?", viewsToAdd, chapterId);
                                log.info("Synced chapter {} with {} new views. Rows affected: {}", chapterId, viewsToAdd, updated);
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("Failed to sync chapter view for key: " + key, e);
                }
            }
        }
        
        log.info("Finished view count sync cycle.");
    }
}
