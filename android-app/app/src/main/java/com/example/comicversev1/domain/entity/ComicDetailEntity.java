package com.example.comicversev1.domain.entity;

import java.util.List;

public class ComicDetailEntity {
    private final int id;
    private final String slug;
    private final String title;
    private final String coverImage;
    private final String aiSummary;
    private final String synopsis;
    private final String authorName;
    private final String status;
    private final String updatedAt;
    private final int viewCount;
    private final List<String> genres;

    public ComicDetailEntity(int id, String slug, String title, String coverImage, String aiSummary, String synopsis, String authorName, String status, String updatedAt, int viewCount, List<String> genres) {
        this.id = id;
        this.slug = slug;
        this.title = title;
        this.coverImage = coverImage;
        this.aiSummary = aiSummary;
        this.synopsis = synopsis;
        this.authorName = authorName;
        this.status = status;
        this.updatedAt = updatedAt;
        this.viewCount = viewCount;
        this.genres = genres;
    }

    public int getId() { return id; }
    public String getSlug() { return slug; }
    public String getTitle() { return title; }
    public String getCoverImage() { return coverImage; }
    public String getAiSummary() { return aiSummary; }
    public String getSynopsis() { return synopsis; }
    public String getAuthorName() { return authorName; }
    public String getStatus() { return status; }
    public String getUpdatedAt() { return updatedAt; }
    public int getViewCount() { return viewCount; }
    public List<String> getGenres() { return genres; }
}

