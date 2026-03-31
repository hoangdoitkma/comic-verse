package com.datn.backend.service;

import jakarta.servlet.http.HttpServletRequest;

public interface TrackingService {
    void trackChapterView(Integer comicId, Integer chapterId, HttpServletRequest request);
}
