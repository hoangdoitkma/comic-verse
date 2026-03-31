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
     * Cron Job: Runs every 10 minutes to sync view count from Redis back to MySQL
     */
    @Scheduled(cron = "0 0/10 * * * ?")
    @Transactional
    public void syncViewsToDatabase() {
        Set<String> keys = redisTemplate.keys("view_count:comic:*");
        
        if (keys == null || keys.isEmpty()) {
            return; // No pending views
        }

        log.info("Start syncing {} comic view counts from Redis to MySQL", keys.size());

        for (String key : keys) {
            try {
                // Get current value and then set back to 0 (or decrement)
                // Using getAndSet is atomic and prevents losing views that are added concurrently
                String countStr = redisTemplate.opsForValue().getAndDelete(key);
                
                if (countStr != null) {
                    int viewsToAdd = Integer.parseInt(countStr);
                    if (viewsToAdd > 0) {
                        String[] parts = key.split(":");
                        if (parts.length >= 3) {
                            int comicId = Integer.parseInt(parts[2]);
                            
                            // Safe batch update
                            int updated = jdbcTemplate.update("UPDATE comic SET views = views + ? WHERE id = ?", viewsToAdd, comicId);
                            log.info("Synced comic {} with {} new views. Rows affected: {}", comicId, viewsToAdd, updated);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Failed to sync view for key: " + key, e);
            }
        }
        
        log.info("Finished view count sync cycle.");
    }
}
